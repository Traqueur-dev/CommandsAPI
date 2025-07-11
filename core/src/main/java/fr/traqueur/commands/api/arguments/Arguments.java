package fr.traqueur.commands.api.arguments;

import fr.traqueur.commands.api.exceptions.ArgumentNotExistException;
import fr.traqueur.commands.api.exceptions.NoGoodTypeArgumentException;
import fr.traqueur.commands.api.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is used to store arguments.
 */
public class Arguments {

    /**
     * The map of the arguments.
     */
    private final Map<String, ArgumentValue> arguments;

    /**
     * The logger of the class.
     */
    private final Logger logger;

    /**
     * Constructor of the class.
     * @param logger The logger of the class.
     */
    public Arguments(Logger logger) {
        this.arguments = new HashMap<>();
        this.logger = logger;
    }

    /**
     * Get an argument from the map.
     *
     * @param argument The key of the argument.
     * @param <T> The type of the argument.
     * @return The argument.
     */
    public <T> T get(String argument) {
        try {
            Optional<T> value = this.getOptional(argument);
            if (!value.isPresent()) {
                throw new ArgumentNotExistException();
            }
            return value.get();
        } catch (ArgumentNotExistException e) {
            logger.error("The argument " + argument + " does not exist.");
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * Get an argument from the map as an integer.
     *
     * @param argument The key of the argument.
     * @param defaultValue The default value to return if the argument is not present.
     * @return The integer or the default value if not present.
     */
    public int getAsInt(String argument, int defaultValue) {
        Optional<Integer> value = this.getAsInt(argument);
        return value.orElse(defaultValue);
    }

    /**
     * Get an argument from the map as a double.
     *
     * @param argument The key of the argument.
     * @param defaultValue The default value to return if the argument is not present.
     * @return The double or the default value if not present.
     */
    public double getAsDouble(String argument, double defaultValue) {
        Optional<Double> value = this.getAsDouble(argument);
        return value.orElse(defaultValue);
    }

    /**
     * Get an argument from the map as a boolean.
     *
     * @param argument The key of the argument.
     * @param defaultValue The default value to return if the argument is not present.
     * @return The boolean or the default value if not present.
     */
    public boolean getAsBoolean(String argument, boolean defaultValue) {
        Optional<Boolean> value = this.getAsBoolean(argument);
        return value.orElse(defaultValue);
    }

    /**
     * Get an argument from the map as a string.
     *
     * @param argument The key of the argument.
     * @param defaultValue The default value to return if the argument is not present.
     * @return The string or the default value if not present.
     */
    public String getAsString(String argument, String defaultValue) {
        Optional<String> value = this.getAsString(argument);
        return value.orElse(defaultValue);
    }

    /**
     * Get an argument from the map as a long.
     *
     * @param argument The key of the argument.
     * @param defaultValue The default value to return if the argument is not present.
     * @return The long or the default value if not present.
     */
    public long getAsLong(String argument, long defaultValue) {
        Optional<Long> value = this.getAsLong(argument);
        return value.orElse(defaultValue);
    }

    /**
     * Get an argument from the map as a float.
     *
     * @param argument The key of the argument.
     * @param defaultValue The default value to return if the argument is not present.
     * @return The float or the default value if not present.
     */
    public float getAsFloat(String argument, float defaultValue) {
        Optional<Float> value = this.getAsFloat(argument);
        return value.orElse(defaultValue);
    }

    /**
     * Get an argument from the map as a short.
     *
     * @param argument The key of the argument.
     * @param defaultValue The default value to return if the argument is not present.
     * @return The short or the default value if not present.
     */
    public short getAsShort(String argument, short defaultValue) {
        Optional<Short> value = this.getAsShort(argument);
        return value.orElse(defaultValue);
    }

    /**
     * Get an argument from the map as a byte.
     *
     * @param argument The key of the argument.
     * @param defaultValue The default value to return if the argument is not present.
     * @return The byte or the default value if not present.
     */
    public byte getAsByte(String argument, byte defaultValue) {
        Optional<Byte> value = this.getAsByte(argument);
        return value.orElse(defaultValue);
    }

    /**
     * Get an argument from the map as a character.
     *
     * @param argument The key of the argument.
     * @param defaultValue The default value to return if the argument is not present.
     * @return The character or the default value if not present.
     */
    public char getAsChar(String argument, char defaultValue) {
        Optional<Character> value = this.getAsChar(argument);
        return value.orElse(defaultValue);
    }

    /**
     * Get an argument from the map as an integer.
     *
     * @param argument The key of the argument.
     * @return The integer or empty if not present.
     */
    public Optional<Integer> getAsInt(String argument) {
       try {
           return this.getAs(argument, String.class).map(Integer::parseInt);
       } catch (NumberFormatException e) {
           return Optional.empty();
       }
    }

    /**
     * Get an argument from the map as a double.
     *
     * @param argument The key of the argument.
     * @return The double or empty if not present.
     */
    public Optional<Double> getAsDouble(String argument) {
        try {
            return this.getAs(argument, String.class).map(Double::parseDouble);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Get an argument from the map as a boolean.
     *
     * @param argument The key of the argument.
     * @return The boolean or empty if not present.
     */
    public Optional<Boolean> getAsBoolean(String argument) {
        return this.getAs(argument, String.class).map(Boolean::parseBoolean);
    }

    /**
     * Get an argument from the map as a string.
     *
     * @param argument The key of the argument.
     * @return The string or empty if not present.
     */
    public Optional<String> getAsString(String argument) {
        return this.getAs(argument, String.class);
    }

    /**
     * Get an argument from the map as a long.
     *
     * @param argument The key of the argument.
     * @return The long or empty if not present.
     */
    public Optional<Long> getAsLong(String argument) {
       try {
           return this.getAs(argument, String.class).map(Long::parseLong);
       } catch (NumberFormatException e) {
           return Optional.empty();
       }
    }

    /**
     * Get an argument from the map as a float.
     *
     * @param argument The key of the argument.
     * @return The float or empty if not present.
     */
    public Optional<Float> getAsFloat(String argument) {
        try {
            return this.getAs(argument, String.class).map(Float::parseFloat);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Get an argument from the map as a short.
     *
     * @param argument The key of the argument.
     * @return The short or empty if not present.
     */
    public Optional<Short> getAsShort(String argument) {
        try {
            return this.getAs(argument, String.class).map(Short::parseShort);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Get an argument from the map as a byte.
     *
     * @param argument The key of the argument.
     * @return The byte or empty if not present.
     */
    public Optional<Byte> getAsByte(String argument) {
        try {
            return this.getAs(argument, String.class).map(Byte::parseByte);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Get an argument from the map as a character.
     *
     * @param argument The key of the argument.
     * @return The character or empty if not present.
     */
    public Optional<Character> getAsChar(String argument) {
        return this.getAs(argument, String.class).map(s -> s.charAt(0));
    }

    /**
     * Get an argument from the map as a specific type.
     *
     * @param argument The key of the argument.
     * @param typeRef The type of the argument.
     * @param defaultValue The default value to return if the argument is not present.
     * @param <T> The type of the argument.
     * @return The argument or the default value if not present.
     */
    public <T> T getAs(String argument, Class<T> typeRef, T defaultValue) {
        Optional<T> value = this.getAs(argument, typeRef);
        return value.orElse(defaultValue);
    }

    /**
     * Get an argument from the map as optional.
     *
     * @param argument The key of the argument.
     * @param typeRef The type of the argument.
     * @param <T> The type of the argument.
     * @return The argument.
     */
    public <T> Optional<T> getAs(String argument, Class<T> typeRef) {
        if(typeRef.isPrimitive()) {
            throw new IllegalArgumentException("The type " + typeRef.getName() + " is a primitive type. You must use the primitive methode");
        }
        if(this.arguments.isEmpty()) {
            return Optional.empty();
        }

        ArgumentValue argumentValue = this.arguments.getOrDefault(argument, null);

        if(argumentValue == null) {
            return Optional.empty();
        }

        Class<?> type = argumentValue.getType();
        Object value = argumentValue.getValue();

        try {
            if(!typeRef.isAssignableFrom(type)) {
                throw new NoGoodTypeArgumentException();
            }
            if (!typeRef.isInstance(value)) {
                throw new NoGoodTypeArgumentException();
            }
        } catch (NoGoodTypeArgumentException e) {
            logger.error("The argument " + argument + " is not the good type.");
            return Optional.empty();
        }

        return Optional.of(typeRef.cast(value));
    }

    /**
     * Get an argument from the map as optional.
     *
     * @param argument The key of the argument.
     * @param <T> The type of the argument.
     * @return The argument.
     */
    public <T> Optional<T> getOptional(String argument) {
        if(this.arguments.isEmpty()) {
            return Optional.empty();
        }

        ArgumentValue argumentValue = this.arguments.getOrDefault(argument, null);

        if(argumentValue == null) {
            return Optional.empty();
        }

        Class<?> type = argumentValue.getType();
        Object value = argumentValue.getValue();
        Class<T> goodType = (Class<T>) type;

        try {
            if (!goodType.isInstance(value)) {
                throw new NoGoodTypeArgumentException();
            }
        } catch (NoGoodTypeArgumentException e) {
            logger.error("The argument " + argument + " is not the good type.");
            logger.error(e.getMessage());
        }

        return Optional.ofNullable(goodType.cast(value));
    }

    /**
     * Add an argument to the map.
     *
     * @param key The key of the argument.
     * @param type The type of the argument.
     * @param object The object of the argument.
     */
    public void add(String key, Class<?> type, Object object) {
        ArgumentValue argumentValue = new ArgumentValue(type, object);
        this.arguments.put(key, argumentValue);
    }
}
