package fr.traqueur.commands.api.requirements;

/**
 * The interface Requirement.
 * <p>
 *     This interface is used to represent a requirement for commandsender externally of command execution environement.
 * </p>
 * @param <S> The type of the sender that will use this requirement.
 */
public interface Requirement<S> {

    /**
     * Check if the sender meet the requirement.
     * @param sender The sender
     * @return true if the sender meet the requirement, false otherwise
     */
    boolean check(S sender);

    /**
     * Get the error message if the sender doesn't meet the requirement.
     * @return The error message
     */
    String errorMessage();
}
