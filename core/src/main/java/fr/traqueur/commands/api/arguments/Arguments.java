package fr.traqueur.commands.api.arguments;

import fr.traqueur.commands.api.exceptions.ArgumentNotExistException;
import fr.traqueur.commands.api.exceptions.NoGoodTypeArgumentException;
import fr.traqueur.commands.api.logging.Logger;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * This class is used to store arguments.
 */
public class Arguments {

    /**
     * The map of the arguments.
     */
    protected final Map<String, ArgumentValue> arguments;

    /**
     * The logger of the class.
     */
    protected final Logger logger;

    /**
     * Constructor of the class.
     *
     * @param logger The logger of the class.
     */
    public Arguments(Logger logger) {
        this.arguments = new HashMap<>();
        this.logger = logger;
    }

    /**
     * Convert all arguments to a simple map of String to Object.
     *
     * @return an unmodifiable map containing argument names as keys and their values
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        arguments.forEach((k, v) -> result.put(k, v.value()));
        return result;
    }

    /**
     * Get the number of arguments stored.
     *
     * @return the number of arguments
     */
    public int size() {
        return arguments.size();
    }

    /**
     * Check if there are no arguments stored.
     *
     * @return true if there are no arguments, false otherwise
     */
    public boolean isEmpty() {
        return arguments.isEmpty();
    }

    /**
     * Get all argument keys.
     *
     * @return an unmodifiable set of argument names
     */
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(arguments.keySet());
    }

    /**
     * Iterate over all arguments and apply the given action.
     *
     * @param action the action to apply to each argument (key, value)
     */
    public void forEach(BiConsumer<String, Object> action) {
        arguments.forEach((k, v) -> action.accept(k, v.value()));
    }

    /**
     * Get an argument from the map.
     *
     * @param argument The key of the argument.
     * @param <T>      The type of the argument.
     * @return The argument.
     */
    public <T> T get(String argument) {
        return this.<T>getOptional(argument).orElseThrow(ArgumentNotExistException::new);
    }


    /**
     * Get an argument from the map as optional.
     *
     * @param argument The key of the argument.
     * @param <T>      The type of the argument.
     * @return The argument.
     */
     @SuppressWarnings("unchecked")
    public <T> Optional<T> getOptional(String argument) {
        if (this.isEmpty()) {
            return Optional.empty();
        }

        ArgumentValue argumentValue = this.arguments.getOrDefault(argument, null);

        if (argumentValue == null) {
            return Optional.empty();
        }

        Class<?> type = argumentValue.type();
        Object value = argumentValue.value();
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
     * @param key    The key of the argument.
     * @param type   The type of the argument.
     * @param object The object of the argument.
     */
    public <T> void add(String key, Class<T> type, T object) {
        ArgumentValue argumentValue = new ArgumentValue(type, object);
        this.arguments.put(key, argumentValue);
    }

    /**
     * Check if an argument exists in the map.
     *
     * @param key The key of the argument.
     * @return true if the argument exists, false otherwise.
     */
    public boolean has(String key) {
        return this.arguments.containsKey(key);
    }

    /**
     * Get the logger of the class.
     *
     * @return The logger of the class.
     */
    protected Logger getLogger() {
        return this.logger;
    }
}
