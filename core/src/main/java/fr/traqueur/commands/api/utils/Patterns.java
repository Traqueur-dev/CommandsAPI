package fr.traqueur.commands.api.utils;

import java.util.regex.Pattern;

/**
 * Utility class providing commonly used compiled regex patterns.
 * Using compiled patterns is more efficient than calling String.split() with a regex.
 */
public final class Patterns {

    /**
     * Pattern for splitting strings on dots.
     */
    public static final Pattern DOT = Pattern.compile("\\.");

    private Patterns() {
        // Utility class
    }
}
