package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.ArgumentValue;
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

    public int getAsInt(String argument) {
        Optional<Integer> value = this.getOptionalAsInt(argument);
        try {
            if (!value.isPresent()) {
                throw new ArgumentNotExistException();
            }
        } catch (ArgumentNotExistException e) {
            logger.error("The argument " + argument + " does not exist.");
            logger.error(e.getMessage());
            return -1;
        }
        return value.get();
    }

    public double getAsDouble(String argument) {
        Optional<Double> value = this.getOptionalAsDouble(argument);
        try {
            if (!value.isPresent()) {
                throw new ArgumentNotExistException();
            }
        } catch (ArgumentNotExistException e) {
            logger.error("The argument " + argument + " does not exist.");
            logger.error(e.getMessage());
            return -1;
        }
        return value.get();
    }

    public boolean getAsBoolean(String argument) {
        Optional<Boolean> value = this.getOptionalAsBoolean(argument);
        try {
            if (!value.isPresent()) {
                throw new ArgumentNotExistException();
            }
        } catch (ArgumentNotExistException e) {
            logger.error("The argument " + argument + " does not exist.");
            logger.error(e.getMessage());
            return false;
        }
        return value.get();
    }

    public String getAsString(String argument) {
        Optional<String> value = this.getOptionalAsString(argument);
        try {
            if (!value.isPresent()) {
                throw new ArgumentNotExistException();
            }
        } catch (ArgumentNotExistException e) {
            logger.error("The argument " + argument + " does not exist.");
            logger.error(e.getMessage());
            return null;
        }
        return value.get();
    }

    public long getAsLong(String argument) {
        Optional<Long> value = this.getOptionalAsLong(argument);
        try {
            if (!value.isPresent()) {
                throw new ArgumentNotExistException();
            }
        } catch (ArgumentNotExistException e) {
            logger.error("The argument " + argument + " does not exist.");
            logger.error(e.getMessage());
            return -1;
        }
        return value.get();
    }

    public float getAsFloat(String argument) {
        Optional<Float> value = this.getOptionalAsFloat(argument);
        try {
            if (!value.isPresent()) {
                throw new ArgumentNotExistException();
            }
        } catch (ArgumentNotExistException e) {
            logger.error("The argument " + argument + " does not exist.");
            logger.error(e.getMessage());
            return -1;
        }
        return value.get();
    }

    public short getAsShort(String argument) {
        Optional<Short> value = this.getOptionalAsShort(argument);
        try {
            if (!value.isPresent()) {
                throw new ArgumentNotExistException();
            }
        } catch (ArgumentNotExistException e) {
            logger.error("The argument " + argument + " does not exist.");
            logger.error(e.getMessage());
            return -1;
        }
        return value.get();
    }

    public byte getAsByte(String argument) {
        Optional<Byte> value = this.getOptionalAsByte(argument);
        try {
            if (!value.isPresent()) {
                throw new ArgumentNotExistException();
            }
        } catch (ArgumentNotExistException e) {
            logger.error("The argument " + argument + " does not exist.");
            logger.error(e.getMessage());
            return -1;
        }
        return value.get();
    }

    public char getAsChar(String argument) {
        Optional<Character> value = this.getOptionalAsChar(argument);
        try {
            if (!value.isPresent()) {
                throw new ArgumentNotExistException();
            }
        } catch (ArgumentNotExistException e) {
            logger.error("The argument " + argument + " does not exist.");
            logger.error(e.getMessage());
            return ' ';
        }
        return value.get();
    }


    public Optional<Integer> getOptionalAsInt(String argument) {
        String value = this.getAs(argument, String.class);
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Double> getOptionalAsDouble(String argument) {
        String value = this.getAs(argument, String.class);
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Boolean> getOptionalAsBoolean(String argument) {
        String value = this.getAs(argument, String.class);
        return Optional.of(Boolean.parseBoolean(value));
    }

    public Optional<String> getOptionalAsString(String argument) {
        return Optional.of(this.getAs(argument, String.class));
    }

    public Optional<Long> getOptionalAsLong(String argument) {
        String value = this.getAs(argument, String.class);
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Float> getOptionalAsFloat(String argument) {
        String value = this.getAs(argument, String.class);
        try {
            return Optional.of(Float.parseFloat(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Short> getOptionalAsShort(String argument) {
        String value = this.getAs(argument, String.class);
        try {
            return Optional.of(Short.parseShort(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Byte> getOptionalAsByte(String argument) {
        String value = this.getAs(argument, String.class);
        try {
            return Optional.of(Byte.parseByte(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Character> getOptionalAsChar(String argument) {
        String value = this.getAs(argument, String.class);
        return Optional.of(value.charAt(0));
    }

    public <T> T getAs(String argument, Class<T> typeRef) {
        try {
            Optional<T> value = this.getOptionalAs(argument, typeRef);
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

    public <T> Optional<T> getOptionalAs(String argument, Class<T> typeRef) {
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
            logger.error(e.getMessage());
        }

        return Optional.ofNullable(typeRef.cast(value));
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
    protected void add(String key, Class<?> type, Object object) {
        ArgumentValue argumentValue = new ArgumentValue(type, object);
        this.arguments.put(key, argumentValue);
    }
}
