package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;

public abstract class Command<T> extends fr.traqueur.commands.api.Command<T, CommandSource> {
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
