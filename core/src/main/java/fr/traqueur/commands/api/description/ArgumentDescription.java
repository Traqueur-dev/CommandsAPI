package fr.traqueur.commands.api.description;

/**
 * A public, read-only description of a command argument.
 *
 * @param name     The name of the argument.
 * @param type     The type the argument is converted to; for an infinite argument,
 *                 {@link fr.traqueur.commands.api.arguments.Infinite}.
 * @param optional Whether the sender can leave it out.
 * @param infinite Whether it takes the rest of the line.
 * @since 5.7.0
 */
public record ArgumentDescription(String name, Class<?> type, boolean optional, boolean infinite) {

    /**
     * The simple, lower-case name of the type, as shown in the generated usages ({@code integer}).
     *
     * @return The type name.
     */
    public String typeName() {
        return this.type.getSimpleName().toLowerCase(java.util.Locale.ROOT);
    }
}
