package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.ArgumentKey;
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
    private final HashMap<ArgumentKey<?>, Object> arguments;

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
            if (value.isEmpty()) {
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

                Class<T> goodType = (Class<T>) type;

                if (!goodType.isInstance(value)) {
                    throw new NoGoodTypeArgumentException();
                }

                return Optional.of(goodType.cast(value));
            }
            return Optional.empty();
        } catch (NoGoodTypeArgumentException e) {
            logger.error("The argument " + argument + " is not the good type.");
            logger.error(e.getMessage());
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
