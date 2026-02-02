package fr.traqueur.commands.impl.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;

import java.util.function.Function;

/**
 * Base class for numeric argument converters.
 * Provides common null/empty checking and NumberFormatException handling.
 *
 * @param <T> the numeric type to convert to
 */
public class NumberArgument<T extends Number> implements ArgumentConverter<T> {

    private final Function<String, T> parser;

    /**
     * Creates a new number argument converter.
     *
     * @param parser the function to parse the string into the target number type
     */
    public NumberArgument(Function<String, T> parser) {
        this.parser = parser;
    }

    /**
     * Converts a string to a number.
     *
     * @param input the string to convert
     * @return the parsed number, or null if the string is null, empty, or not a valid number
     */
    @Override
    public T apply(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        try {
            return parser.apply(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
