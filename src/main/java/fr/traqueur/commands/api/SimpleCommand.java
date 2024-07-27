package fr.traqueur.commands.api;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a simple command.
 */
public abstract class SimpleCommand extends Command<JavaPlugin> {

    /**
     * The constructor of the command.
     *
     * @param plugin The plugin that owns the command.
     * @param name   The name of the command.
     */
    public SimpleCommand(JavaPlugin plugin, String name) {
        super(plugin, name);
    }
}
