package fr.traqueur.commands.api.arguments;

/**
 * Represents a key for an argument.
 *
 * @param <T> the type of the argument
 */
public class ArgumentKey<T> {

    private final String name;
    private final Class<T> type;

    /**
     * Constructs a new argument key with the given name and type.
     *
     * @param name the name of the argument
     * @param type the type of the argument
     */
    private ArgumentKey(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Creates a new argument key with the given name and type.
     * @param name the name of the argument
     * @param type the type of the argument
     * @return the created argument key
     * @param <T> the type of the argument
     */
    public static <T> ArgumentKey<T> of(String name, Class<T> type) {
        return new ArgumentKey<>(name, type);
    }

    /**
     * Gets the name of the argument.
     *
     * @return the name of the argument
     */
    public String getKey() {
        return name;
    }

    /**
     * Gets the type of the argument.
     *
     * @return the type of the argument
     */
    public Class<T> getType() {
        return type;
    }
}
