package fr.traqueur.commands.api.arguments;

import java.util.function.Function;

/**
 * The class ArgumentConverter.
 * <p> This class is used to convert a string to an object. </p>
 * @param <T> The type of the object.
 */
public interface ArgumentConverter<T> extends Function<String, T> {

    /**
     * Apply the conversion.
     * @param s The string to convert.
     * @return The object.
     */
    @Override
    T apply(String s);
}
