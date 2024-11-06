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
     * This method is used to get the to many args message.
     * For backward compatibility with older versions of the API. The default implementation is the same as getArgNotRecognized.
     * @return The to many args message.
     */
    default String getToManyArgsMessage() {
        return getArgNotRecognized();
    }

    /**
     * This method is used to get a message by a type.
     * @param type The type of the message.
     * @return The message.
     */
    default String getMessage(Messages type) {
        switch (type) {
            case NO_PERMISSION:
                return getNoPermissionMessage();
            case ONLY_IN_GAME:
                return getOnlyInGameMessage();
            case MISSING_ARGS:
                return getMissingArgsMessage();
            case ARG_NOT_RECOGNIZED:
                return getArgNotRecognized();
            case REQUIREMENT_ERROR:
                return getRequirementMessage();
            case TO_MANY_ARGS:
                return getToManyArgsMessage();
        }
        throw new IllegalArgumentException("The message type " + type + " is not supported.");
    }
}
