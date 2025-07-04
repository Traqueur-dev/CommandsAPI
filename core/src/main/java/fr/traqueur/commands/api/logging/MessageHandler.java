package fr.traqueur.commands.api.logging;

/**
 * The class MessageHandler.
 * <p>
 *     This class is used to represent a message handler.
 * </p>
 */
public interface MessageHandler {

    /**
     * This method is used to get the no permission message.
     * @return The no permission message.
     */
    String getNoPermissionMessage();

    /**
     * This method is used to get the only in game message.
     * @return The only in game message.
     */
    String getOnlyInGameMessage();

    /**
     * This method is used to get the arg not recognized message.
     * @return The arg not recognized message.
     */
    String getArgNotRecognized();

    /**
     * This method is used to get the requirement message.
     * @return The requirement message.
     */
    String getRequirementMessage();
}
