package fr.traqueur.commands.api.arguments;

import java.util.function.Function;

/**
 * The class ArgumentConverter.
 * <p> This class is used to convert a string to an object. </p>
 *
 * @param <T> The type of the object.
 */
@FunctionalInterface
public interface ArgumentConverter<T> extends Function<String, T> {

    /**
     * Apply the conversion.
     *
     * @param s The string to convert.
     * @return The object.
     */
    @Override
    T apply(String s);

    record Wrapper<T>(Class<T> clazz, ArgumentConverter<T> converter) {

        /**
         * Convert primitive types to their wrapper equivalents.
         * This is necessary because Arguments storage uses Class.isInstance(),
         * which doesn't work with primitive types (they have no instances).
         *
         * @param type the type to normalize
         * @return the wrapper type if input was primitive, otherwise the input type unchanged
         */
        @SuppressWarnings("unchecked")
        private static <T> Class<T> toWrapperType(Class<T> type) {
            if (!type.isPrimitive()) {
                return type;
            }

            if (type == int.class) return (Class<T>) Integer.class;
            if (type == long.class) return (Class<T>) Long.class;
            if (type == double.class) return (Class<T>) Double.class;
            if (type == float.class) return (Class<T>) Float.class;
            if (type == boolean.class) return (Class<T>) Boolean.class;
            if (type == byte.class) return (Class<T>) Byte.class;
            if (type == short.class) return (Class<T>) Short.class;
            if (type == char.class) return (Class<T>) Character.class;
            if (type == void.class) return (Class<T>) Void.class;

            return type;
        }

        public boolean convertAndApply(String input, String name, Arguments arguments) {
            T result = converter.apply(input);
            if (result == null) {
                return false;
            }
            // Always store with wrapper type to ensure Class.isInstance() works correctly
            Class<T> storageType = toWrapperType(clazz);
            arguments.add(name, storageType, result);
            return true;
        }

    }

}
