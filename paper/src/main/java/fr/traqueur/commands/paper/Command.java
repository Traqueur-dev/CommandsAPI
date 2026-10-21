package fr.traqueur.commands.paper;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A command of the Paper platform.
 *
 * @param <T> The type of the plugin.
 * @since 5.6.0
 */
public abstract class Command<T extends JavaPlugin> extends fr.traqueur.commands.api.models.Command<T, CommandSender> {

    /**
     * Create a command.
     *
     * @param plugin The plugin.
     * @param name   The name of the command.
     */
    public Command(T plugin, String name) {
        super(plugin, name);
    }
}
