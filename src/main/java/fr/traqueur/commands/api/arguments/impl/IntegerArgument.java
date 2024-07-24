package fr.traqueur.commands.api.arguments.impl;


import fr.traqueur.commands.api.arguments.ArgumentConverter;

/**
 * Argument used to convert a string to an integer.
 */
public class IntegerArgument implements ArgumentConverter<Integer> {

    /**
     * Converts a string to an integer.
     *
     * @param input the string to convert
     * @return the integer, or null if the string is not a valid integer
     */
    @Override
    public Integer apply(String input) {
        try {
            return Integer.valueOf(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
