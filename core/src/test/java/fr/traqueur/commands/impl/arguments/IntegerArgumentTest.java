package fr.traqueur.commands.impl.arguments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntegerArgumentTest {

    private IntegerArgument converter;

    @BeforeEach
    void setUp() {
        converter = new IntegerArgument();
    }

    @Test
    void testApply_valid() {
        assertEquals(0, converter.apply("0"));
        assertEquals(123, converter.apply("123"));
        assertEquals(-42, converter.apply("-42"));
    }

    @Test
    void testApply_invalid() {
        assertNull(converter.apply("abc"));
        assertNull(converter.apply("12.3"));
        assertNull(converter.apply(""));
        assertNull(converter.apply(null));
    }
}