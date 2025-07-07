package fr.traqueur.commands.impl.arguments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BooleanArgumentTest {

    private BooleanArgument<Object> converter;

    @BeforeEach
    void setUp() {
        converter = new BooleanArgument<>();
    }

    @Test
    void testApply_validTrueFalse() {
        assertTrue(converter.apply("true"));
        assertFalse(converter.apply("false"));
        assertTrue(converter.apply("TRUE"));
        assertFalse(converter.apply("FaLsE"));
    }

    @Test
    void testApply_invalid() {
        assertNull(converter.apply("yes"));
        assertNull(converter.apply("0"));
        assertNull(converter.apply(""));
        assertNull(converter.apply(null));
    }

    @Test
    void testOnCompletion() {
        List<String> completions = converter.onCompletion(null, Collections.emptyList());
        assertEquals(2, completions.size());
        assertTrue(completions.contains("true"));
        assertTrue(completions.contains("false"));
    }
}