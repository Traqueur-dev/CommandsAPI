package fr.traqueur.commands.api.requirements;

/**
 * The interface Requirement.
 * <p>
 * This interface is used to represent a requirement for commandsender externally of command execution environement.
 * </p>
 *
 * @param <S> The type of the sender that will use this requirement.
 */
public interface Requirement<S> {

    /**
     * Check if the sender meet the requirement.
     *
     * @param sender The sender
     * @return true if the sender meet the requirement, false otherwise
     */
    boolean check(S sender);

    /**
     * Get the error message if the sender doesn't meet the requirement.
     *
     * @return The error message
     */
    String errorMessage();

    /**
     * Get the name of the requirement, shown in the generic requirement message.
     * <p>
     * When {@link #errorMessage()} is empty, the library sends the generic message of the
     * {@link fr.traqueur.commands.api.logging.MessageHandler} and fills its
     * {@code %requirement%} placeholder with this name. Override it to give the sender
     * something more readable than a class name.
     * </p>
     *
     * @return The name of the requirement, the simple name of its class by default.
     * @since 5.4.0
     */
    default String name() {
        return this.getClass().getSimpleName();
    }
}
