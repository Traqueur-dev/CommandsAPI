package fr.traqueur.commands.api.lang;

import fr.traqueur.commands.api.updater.Updater;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * The lang class
 */
public class Lang {

    private static MessageHandler handler;

//    /**
//     * The messages
//     */
//    private static final Map<String, String> messages = new HashMap<>();
//
//    /**
//     * Setup the messages
//     */
//    public static void setupMessages(JavaPlugin plugin) {
//        File file = new File(plugin.getDataFolder(), "commands.yml");
//        YamlConfiguration commandsFile = YamlConfiguration.loadConfiguration(file);
//
//        for (Messages message : Messages.values()) {
//            if(commandsFile.contains(message.getKey())) {
//                messages.put(message.getKey(), commandsFile.getString(message.getKey()));
//            } else {
//                throw new RuntimeException("The key " + message.getKey() + " is missing in the file commands.yml");
//            }
//        }
//    }

    /**
     * Translate a message
     * @param message The message to translate
     * @return The translated message
     */
    public static String translate(Messages message) {
        return ChatColor.translateAlternateColorCodes('&', handler.getMessage(message));
    }

    public static void setMessageHandler(MessageHandler handler) {
        Lang.handler = handler;
    }
}
