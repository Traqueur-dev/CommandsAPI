package fr.traqueur.commands.impl.arguments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LongArgumentTest {

    private LongArgument converter;

    @BeforeEach
    void setUp() {
        converter = new LongArgument();
    }

    @Test
    void testApply_valid() {
        assertEquals(0L, converter.apply("0"));
        assertEquals(1234567890123L, converter.apply("1234567890123"));
        assertEquals(-9876543210L, converter.apply("-9876543210"));
    }

    @Test
    void testApply_invalid() {
        assertNull(converter.apply("abc"));
        assertNull(converter.apply("12.34"));
        assertNull(converter.apply(""));
        assertNull(converter.apply(null));
    }
}