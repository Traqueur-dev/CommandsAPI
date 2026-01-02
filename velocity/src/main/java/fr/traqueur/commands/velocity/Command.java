package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;

/**
 * This implementation of {@link fr.traqueur.commands.api.models.Command} is used to provide a command in Spigot.
 *
 * @param <T> is the type of the plugin, which must extend the main plugin class.
 */
public abstract class Command<T> extends fr.traqueur.commands.api.models.Command<T, CommandSource> {
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
