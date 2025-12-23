package fr.traqueur.commands.api.arguments;

import java.util.function.Function;

/**
 * The class ArgumentConverter.
 * <p> This class is used to convert a string to an object. </p>
 * @param <T> The type of the object.
 */
@FunctionalInterface
public interface ArgumentConverter<T> extends Function<String, T> {

    /**
     * Apply the conversion.
     * @param s The string to convert.
     * @return The object.
     */
    @Override
    T apply(String s);

    record Wrapper<T>(Class<T> clazz, ArgumentConverter<T> converter) {

        public boolean convertAndApply(String input, String name, Arguments arguments) {
            T result = converter.apply(input);
            if (result == null) {
                return false;
            }
            arguments.add(name, clazz, result);
            return true;
        }

    }

}
