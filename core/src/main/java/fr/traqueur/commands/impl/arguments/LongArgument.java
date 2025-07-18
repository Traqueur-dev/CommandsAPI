package fr.traqueur.commands.impl.arguments;


import fr.traqueur.commands.api.arguments.ArgumentConverter;

/**
 * Argument used to convert a string to a long.
 */
public class LongArgument implements ArgumentConverter<Long> {

    /**
     * Default constructor.
     */
    public LongArgument() {}

    /**
     * Convert a string to a long.
     *
     * @param input the string to convert
     * @return the long or null if the string is not a long
     */
    @Override
    public Long apply(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
