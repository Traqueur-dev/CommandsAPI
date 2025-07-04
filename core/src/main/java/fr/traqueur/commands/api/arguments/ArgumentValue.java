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

    public ArgumentValue(Class<?> type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Class<?> getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

}
