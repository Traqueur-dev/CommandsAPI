package fr.traqueur.commands.api.arguments;

/**
 * Represents a value of an argument with its type.
 * This class is used to store the type and value of an argument.
 */
public class ArgumentValue {

    /**
     * The type of the argument.
     */
    private final Class<?> type;
    /**
     * The value of the argument.
     */
    private final Object value;

    /**
     * Constructor to create an ArgumentValue with a specified type and value.
     *
     * @param type  The class type of the argument.
     * @param value The value of the argument.
     */
    public ArgumentValue(Class<?> type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Get the type of the argument.
     *
     * @return The type of the argument.
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * Get the value of the argument.
     *
     * @return The value of the argument.
     */
    public Object getValue() {
        return this.value;
    }

}
