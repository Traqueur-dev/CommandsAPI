package fr.traqueur.commands.api.messages;

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
     * This method is used to get the missing args message.
     * @return The missing args message.
     */
    String getMissingArgsMessage();

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

    /**
     * This method is used to get a message by a type.
     * @param type The type of the message.
     * @return The message.
     */
    default String getMessage(Messages type) {
        return switch (type) {
            case NO_PERMISSION -> getNoPermissionMessage();
            case ONLY_IN_GAME -> getOnlyInGameMessage();
            case MISSING_ARGS -> getMissingArgsMessage();
            case ARG_NOT_RECOGNIZED -> getArgNotRecognized();
            case REQUIREMENT_ERROR -> getRequirementMessage();
        };
    }
}
