package fr.traqueur.commands.impl.arguments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoubleArgumentTest {
    private DoubleArgument converter;

    @BeforeEach
    void setUp() {
        converter = new DoubleArgument();
    }

    @Test
    void testApply_valid() {
        assertEquals(3.14, converter.apply("3.14"));
        assertEquals(-2.718, converter.apply("-2.718"));
        assertEquals(0.0, converter.apply("0"));
    }

    @Test
    void testApply_invalid() {
        assertNull(converter.apply("abc"));
        assertNull(converter.apply(null));
    }
}