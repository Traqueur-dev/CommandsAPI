package fr.traqueur.commands.impl.arguments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnumArgumentTest {

    private enum Sample {ONE, TWO, THREE}
    private EnumArgument<Sample, Object> converter;

    @BeforeEach
    void setUp() {
        converter = EnumArgument.of(Sample.class);
    }

    @Test
    void testApply_valid() {
        assertEquals(Sample.ONE, converter.apply("ONE"));
        assertEquals(Sample.TWO, converter.apply("TWO"));
    }

    @Test
    void testApply_invalid() {
        assertNull(converter.apply("one")); // case-sensitive
        assertNull(converter.apply("FOUR"));
        assertNull(converter.apply(""));
        assertNull(converter.apply(null));
    }

    @Test
    void testOnCompletion() {
        List<String> completions = converter.onCompletion(null, Collections.emptyList());
        assertEquals(3, completions.size());
        assertTrue(completions.contains("ONE"));
        assertTrue(completions.contains("TWO"));
        assertTrue(completions.contains("THREE"));
    }
}
