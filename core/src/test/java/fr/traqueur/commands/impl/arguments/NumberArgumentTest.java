package fr.traqueur.commands.impl.arguments;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NumberArgumentTest {

    @Test
    void testInteger_valid() {
        var converter = new NumberArgument<>(Integer::valueOf);
        assertEquals(0, converter.apply("0"));
        assertEquals(123, converter.apply("123"));
        assertEquals(-42, converter.apply("-42"));
    }

    @Test
    void testInteger_invalid() {
        var converter = new NumberArgument<>(Integer::valueOf);
        assertNull(converter.apply("abc"));
        assertNull(converter.apply("12.3"));
        assertNull(converter.apply(""));
        assertNull(converter.apply(null));
    }

    @Test
    void testLong_valid() {
        var converter = new NumberArgument<>(Long::valueOf);
        assertEquals(0L, converter.apply("0"));
        assertEquals(1234567890123L, converter.apply("1234567890123"));
        assertEquals(-9876543210L, converter.apply("-9876543210"));
    }

    @Test
    void testLong_invalid() {
        var converter = new NumberArgument<>(Long::valueOf);
        assertNull(converter.apply("abc"));
        assertNull(converter.apply("12.34"));
        assertNull(converter.apply(""));
        assertNull(converter.apply(null));
    }

    @Test
    void testDouble_valid() {
        var converter = new NumberArgument<>(Double::valueOf);
        assertEquals(3.14, converter.apply("3.14"));
        assertEquals(-2.718, converter.apply("-2.718"));
        assertEquals(0.0, converter.apply("0"));
    }

    @Test
    void testDouble_invalid() {
        var converter = new NumberArgument<>(Double::valueOf);
        assertNull(converter.apply("abc"));
        assertNull(converter.apply(null));
    }

    @Test
    void testFloat_valid() {
        var converter = new NumberArgument<>(Float::valueOf);
        assertEquals(3.14f, converter.apply("3.14"));
        assertEquals(-2.718f, converter.apply("-2.718"));
        assertEquals(0.0f, converter.apply("0"));
    }

    @Test
    void testFloat_invalid() {
        var converter = new NumberArgument<>(Float::valueOf);
        assertNull(converter.apply("abc"));
        assertNull(converter.apply(null));
    }

    @Test
    void testByte_valid() {
        var converter = new NumberArgument<>(Byte::valueOf);
        assertEquals((byte) 0, converter.apply("0"));
        assertEquals((byte) 127, converter.apply("127"));
        assertEquals((byte) -128, converter.apply("-128"));
    }

    @Test
    void testByte_invalid() {
        var converter = new NumberArgument<>(Byte::valueOf);
        assertNull(converter.apply("abc"));
        assertNull(converter.apply("128")); // overflow
        assertNull(converter.apply(""));
        assertNull(converter.apply(null));
    }
}
