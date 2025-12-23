package fr.traqueur.commands.api.arguments;

import fr.traqueur.commands.api.exceptions.ArgumentNotExistException;
import fr.traqueur.commands.impl.logging.InternalLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
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

}