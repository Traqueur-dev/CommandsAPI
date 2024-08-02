package fr.traqueur.commands.api.messages;

import org.bukkit.ChatColor;

/**
 * The class Messages.
 * <p>
 *     This class is used to represent the messages of the plugin.
 * </p>
 */
public enum Messages {

    /**
     * The message when the player has no permission.
     */
    NO_PERMISSION,

    /**
     * The message when the sender is not in game.
     */
    ONLY_IN_GAME,

    /**
     * The message when command missing args.
     */
    MISSING_ARGS,

    /**
     * The message when an arg command is not recognized.
     */
    ARG_NOT_RECOGNIZED,

    /**
     * The message when a requirement is not met.
     */
    REQUIREMENT_ERROR,
    ;

    /**
     * The message handler
     */
    private static MessageHandler handler;

    /**
     * Translate a message
     * @return The translated message
     */
    public String message() {
        return ChatColor.translateAlternateColorCodes('&', handler.getMessage(this));
    }

    /**
     * Set the message handler
     * @param handler The message handler
     */
    public static void setMessageHandler(MessageHandler handler) {
        Messages.handler = handler;
    }

}
