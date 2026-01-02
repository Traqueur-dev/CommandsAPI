package fr.traqueur.commands.api.arguments;

/**
 * Represents a value of an argument with its type.
 * This class is used to store the type and value of an argument.
 *
 * @param type  The type of the argument.
 * @param value The value of the argument.
 */
public record ArgumentValue(Class<?> type, Object value) {

    /**
     * Get the type of the argument.
     *
     * @return The type of the argument.
     */
    @Override
    public Class<?> type() {
        return this.type;
    }

    /**
     * Get the value of the argument.
     *
     * @return The value of the argument.
     */
    @Override
    public Object value() {
        return this.value;
    }

}
