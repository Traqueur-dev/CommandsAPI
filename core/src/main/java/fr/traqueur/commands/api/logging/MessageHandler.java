package fr.traqueur.commands.api.logging;

/**
 * The class MessageHandler.
 * <p>
 * This class is used to represent a message handler.
 * </p>
 */
public interface MessageHandler {

    /**
     * This method is used to get the no permission message.
     *
     * @return The no permission message.
     */
    String getNoPermissionMessage();

    /**
     * This method is used to get the only in game message.
     *
     * @return The only in game message.
     */
    String getOnlyInGameMessage();

    /**
     * This method is used to get the arg not recognized message.
     *
     * @return The arg not recognized message.
     */
    String getArgNotRecognized();

    /**
     * This method is used to get the requirement message.
     *
     * @return The requirement message.
     */
    String getRequirementMessage();

    /**
     * This method is used to get the command disabled message.
     *
     * @return The command disabled message.
     */
    String getCommandDisabledMessage();

    /**
     * This method is used to get the argument too long message.
     *
     * @return The argument too long message.
     */
    String getArgumentTooLongMessage();

    /**
     * This method is used to get the invalid format message.
     *
     * @return The invalid format message.
     */
    String getInvalidFormatMessage();

    /**
     * Get the text of a message by its key.
     * <p>
     * The text still holds its placeholders ({@code %arg%}, {@code %requirement%}). Handy for a
     * {@link MessageDispatcher} or a component formatter that keeps its texts in the handler.
     * </p>
     *
     * @param key The message.
     * @return The text of the message, or null for {@link MessageKey#USAGE} and
     * {@link MessageKey#INTERNAL_ERROR}, which the handler does not hold.
     * @since 5.5.0
     */
    default String getMessage(MessageKey key) {
        return switch (key) {
            case NO_PERMISSION -> getNoPermissionMessage();
            case ONLY_IN_GAME -> getOnlyInGameMessage();
            case ARG_NOT_RECOGNIZED -> getArgNotRecognized();
            case REQUIREMENT -> getRequirementMessage();
            case DISABLED -> getCommandDisabledMessage();
            case TOO_LONG -> getArgumentTooLongMessage();
            case INVALID_FORMAT -> getInvalidFormatMessage();
            case USAGE, INTERNAL_ERROR -> null;
        };
    }
}
