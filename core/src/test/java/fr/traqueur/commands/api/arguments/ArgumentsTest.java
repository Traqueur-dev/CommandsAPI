package fr.traqueur.commands.api.arguments;

import fr.traqueur.commands.api.exceptions.ArgumentNotExistException;
import fr.traqueur.commands.impl.logging.InternalLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentsTest {

    private Arguments args;

    @BeforeEach
    void setUp() {
        this.args = new Arguments(new InternalLogger(Logger.getLogger("ArgumentsTest")));
    }

    @Test
    void testGetThrowsArgumentNotExistThrow() {
        assertThrows(ArgumentNotExistException.class, () -> args.get("xxx"));
    }

    @Test
    void testInfiniteArgsBehavior() {
        args.add("all", String.class, "Infinite arguments test");
        String allArgs = args.get("all");
        assertNotNull(allArgs);
        assertEquals("Infinite arguments test", allArgs);
    }

    @Test
    void getOptional_onEmptyMapReturnsEmptyWithoutError() {
        Optional<?> opt = args.getOptional("anything");
        assertTrue(opt.isEmpty());
    }

    // --- New utility methods tests ---

    @Test
    void toMap_returnsMapWithValues() {
        args.add("name", String.class, "Alice");
        args.add("age", Integer.class, 25);

        Map<String, Object> map = args.toMap();

        assertEquals(2, map.size());
        assertEquals("Alice", map.get("name"));
        assertEquals(25, map.get("age"));
    }

    @Test
    void toMap_emptyArguments_returnsEmptyMap() {
        Map<String, Object> map = args.toMap();
        assertTrue(map.isEmpty());
    }

    @Test
    void size_returnsCorrectCount() {
        assertEquals(0, args.size());

        args.add("a", String.class, "value1");
        assertEquals(1, args.size());

        args.add("b", Integer.class, 42);
        assertEquals(2, args.size());
    }

    @Test
    void isEmpty_returnsTrueWhenEmpty() {
        assertTrue(args.isEmpty());
    }

    @Test
    void isEmpty_returnsFalseWhenNotEmpty() {
        args.add("key", String.class, "value");
        assertFalse(args.isEmpty());
    }

    @Test
    void getKeys_returnsAllKeys() {
        args.add("first", String.class, "a");
        args.add("second", Integer.class, 1);
        args.add("third", Double.class, 1.5);

        Set<String> keys = args.getKeys();

        assertEquals(3, keys.size());
        assertTrue(keys.contains("first"));
        assertTrue(keys.contains("second"));
        assertTrue(keys.contains("third"));
    }

    @Test
    void getKeys_returnsUnmodifiableSet() {
        args.add("key", String.class, "value");

        Set<String> keys = args.getKeys();

        assertThrows(UnsupportedOperationException.class, () -> keys.add("new"));
    }

    @Test
    void forEach_iteratesAllEntries() {
        args.add("a", String.class, "valueA");
        args.add("b", String.class, "valueB");

        AtomicInteger count = new AtomicInteger(0);
        args.forEach((key, value) -> {
            count.incrementAndGet();
            assertTrue(key.equals("a") || key.equals("b"));
        });

        assertEquals(2, count.get());
    }

    @Test
    void has_returnsTrueForExistingKey() {
        args.add("exists", String.class, "value");

        assertTrue(args.has("exists"));
    }

    @Test
    void has_returnsFalseForMissingKey() {
        assertFalse(args.has("missing"));
    }

    @Test
    void get_withDefaultValue_returnsValueWhenPresent() {
        args.add("key", String.class, "actual");

        String result = args.<String>getOptional("key").orElse("default");

        assertEquals("actual", result);
    }

    @Test
    void get_withDefaultValue_returnsDefaultWhenMissing() {
        String result = args.<String>getOptional("missing").orElse("default");

        assertEquals("default", result);
    }
}