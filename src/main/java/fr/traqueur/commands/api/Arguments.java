package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.ArgumentKey;
import fr.traqueur.commands.api.exceptions.ArgumentNotExistException;
import fr.traqueur.commands.api.exceptions.NoGoodTypeArgumentException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is used to store arguments.
 */
public class Arguments {
    private final HashMap<ArgumentKey<?>, Object> arguments;

    /**
     * Constructor of the class.
     */
    public Arguments() {
        this.arguments = new HashMap<>();
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
            if (value.isEmpty()) {
                throw new ArgumentNotExistException();
            }
            return value.get();
        } catch (ArgumentNotExistException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get an argument from the map as optional.
     *
     * @param argument The key of the argument.
     * @param <T> The type of the argument.
     * @return The argument.
     */
    public <T> Optional<T> getOptional(String argument) {
        try {
            for (Map.Entry<ArgumentKey<?>, Object> entry : arguments.entrySet()) {
                ArgumentKey<?> argumentKey = entry.getKey();
                String key = argumentKey.getKey();
                if (!argument.equals(key)) {
                    continue;
                }
                Class<?> type = argumentKey.getType();
                Object value = entry.getValue();

                if (!type.isInstance(value)) {
                    throw new NoGoodTypeArgumentException();
                }

                return Optional.ofNullable((T) value);
            }
            return Optional.empty();
        } catch (NoGoodTypeArgumentException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Add an argument to the map.
     *
     * @param key The key of the argument.
     * @param type The type of the argument.
     * @param object The object of the argument.
     */
    protected void add(String key, Class<?> type, Object object) {
        ArgumentKey<?> argumentKey = ArgumentKey.of(key, type);
        this.arguments.put(argumentKey, object);
    }
}
