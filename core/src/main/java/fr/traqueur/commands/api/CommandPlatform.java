package fr.traqueur.commands.api;

import java.util.logging.Logger;

public interface CommandPlatform<T> {

    T getPlugin();

    void injectManager(CommandManager<T, ?> commandManager);

    Logger getLogger();

    boolean hasPermission(Object sender, String permission);

    void addCommand(Command<T, ?> command, String label);

    void removeCommand(String label, boolean subcommand);
}
