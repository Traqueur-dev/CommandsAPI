package fr.traqueur.commands.api;

import java.util.logging.Logger;

public interface CommandPlatform<T, S> {

    T getPlugin();

    void injectManager(CommandManager<T, S> commandManager);

    Logger getLogger();

    boolean hasPermission(S sender, String permission);

    void addCommand(Command<T, S> command, String label);

    void removeCommand(String label, boolean subcommand);
}
