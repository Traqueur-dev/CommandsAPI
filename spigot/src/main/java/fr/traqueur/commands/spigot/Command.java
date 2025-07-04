package fr.traqueur.commands.spigot;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * {@inheritDoc}
 *
 * This implementation of {@link fr.traqueur.commands.api.Command} is used to provide a command in Spigot.
 */
public abstract class Command<T extends JavaPlugin> extends fr.traqueur.commands.api.Command<T, CommandSender> {

    public Command(T plugin, String name) {
        super(plugin, name);
    }



}
