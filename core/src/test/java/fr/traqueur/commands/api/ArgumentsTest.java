package fr.traqueur.commands.api;
import fr.traqueur.commands.impl.logging.InternalLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ArgumentsTest {

    private InternalLogger logger;
    private Arguments args;

    @BeforeEach
    void setUp() {
        logger = Mockito.mock(InternalLogger.class);
        args = new Arguments(logger);
    }

    @Test
    void testIntCast_validAndInvalid() {
        args.add("num", String.class, "42");
        assertEquals(42, args.getAsInt("num", 0));
        args.add("bad", String.class, "abc");
        assertEquals(0, args.getAsInt("bad", 0));
    }

    @Test
    void testDoubleCast_validAndInvalid() {
        args.add("d", String.class, "3.14");
        assertEquals(3.14, args.getAsDouble("d", 0.0));
        args.add("badD", String.class, "pi");
        assertEquals(0.0, args.getAsDouble("badD", 0.0));
    }

    @Test
    void testBooleanCast() {
        args.add("b1", String.class, "true");
        args.add("b2", String.class, "FALSE");
        assertTrue(args.getAsBoolean("b1", false));
        assertFalse(args.getAsBoolean("b2", true));
    }

    @Test
    void testStringCast_default() {
        assertEquals("def", args.getAsString("missing", "def"));
        args.add("s", String.class, "hello");
        assertEquals("hello", args.getAsString("s", "cfg"));
    }

    @Test
    void testLongFloatShortByteChar() {
        args.add("L", String.class, "1234567890123");
        assertEquals(1234567890123L, args.getAsLong("L", 0L));
        args.add("f", String.class, "2.5");
        assertEquals(2.5f, args.getAsFloat("f", 0f));
        args.add("sh", String.class, "7");
        assertEquals((short)7, args.getAsShort("sh", (short)0));
        args.add("by", String.class, "8");
        assertEquals((byte)8, args.getAsByte("by", (byte)0));
        args.add("c", String.class, "z");
        assertEquals('z', args.getAsChar("c", 'x'));
    }

    @Test
    void testOptionalPresentAndEmpty() {
        args.add("opt", String.class, "val");
        Optional<String> optVal = args.getAsString("opt");
        assertTrue(optVal.isPresent());
        assertEquals("val", optVal.get());
        assertFalse(args.getAsInt("none").isPresent());
    }

    @Test
    void testGetGeneric_andErrorLogging() {
        args.add("gen", Integer.class, 5);
        String wrong = args.getAs("gen", String.class, "def");
        assertEquals("def", wrong);
    }

    @Test
    void testGetThrowsArgumentNotExistLogged() {
        assertNull(args.get("xxx"));
        verify(logger).error(contains("xxx"));
    }

    @Test
    void testInfiniteArgsBehavior() {
        args.add("all", String.class, "Infinite arguments test");
        String allArgs = args.get("all");
        assertNotNull(allArgs);
        assertEquals("Infinite arguments test", allArgs);
    }

    @Test
    void getAs_logsErrorWhenWrongType() {
        InternalLogger mockLogger = Mockito.mock(InternalLogger.class);
        Arguments args = new Arguments(mockLogger);
        args.add("num", Integer.class, 123);
        Optional<String> result = args.getAs("num", String.class);
        assertFalse(result.isPresent());
        verify(mockLogger).error(contains("The argument num is not the good type."));
    }

    @Test
    void getOptional_onEmptyMapReturnsEmptyWithoutError() {
        InternalLogger mockLogger = Mockito.mock(InternalLogger.class);
        Arguments args = new Arguments(mockLogger);
        Optional<?> opt = args.getOptional("anything");
        assertFalse(opt.isPresent());
        verify(mockLogger, never()).error(anyString());
    }
}