package fr.traqueur.commands.api.lang;

import org.bukkit.ChatColor;

/**
 * The lang class
 */
public class Lang {

    /**
     * The message handler
     */
    private static MessageHandler handler;

    /**
     * Translate a message
     * @param message The message to translate
     * @return The translated message
     */
    public static String translate(Messages message) {
        return ChatColor.translateAlternateColorCodes('&', handler.getMessage(message));
    }

    /**
     * Set the message handler
     * @param handler The message handler
     */
    public static void setMessageHandler(MessageHandler handler) {
        Lang.handler = handler;
    }
}
