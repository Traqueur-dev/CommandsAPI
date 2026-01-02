package fr.traqueur.commands.spigot;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This implementation of {@link fr.traqueur.commands.api.models.Command} is used to provide a command in Spigot.
 *
 * @param <T> is the type of the plugin, which must extend the main plugin class.
 */
public abstract class Command<T extends JavaPlugin> extends fr.traqueur.commands.api.models.Command<T, CommandSender> {

    /**
     * The constructor of the command.
     *
     * @param plugin The plugin that owns the command.
     * @param name   The name of the command.
     */
    public Command(T plugin, String name) {
        super(plugin, name);
    }


}
