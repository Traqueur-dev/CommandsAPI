package fr.traqueur.commands.api;

import java.util.logging.Logger;

/** * Represents a command platform.
 * <p> This interface is used to manage commands in a specific platform. </p>
 *
 * @param <T> The type of the plugin.
 * @param <S> The type of the sender.
 */
public interface CommandPlatform<T, S> {

    /**
     * Gets the plugin instance.
     *
     * @return The plugin instance.
     */
    T getPlugin();

    /**
     * Injects the command manager into the platform.
     *
     * @param commandManager The command manager to inject.
     */
    void injectManager(CommandManager<T, S> commandManager);

    /**
     * Gets the logger of the platform.
     *
     * @return The logger of the platform.
     */
    Logger getLogger();

    /**
     * Checks if the sender has a specific permission.
     *
     * @param sender The sender to check.
     * @param permission The permission to check.
     * @return true if the sender has the permission, false otherwise.
     */
    boolean hasPermission(S sender, String permission);

    /**
     * Adds a command to the platform.
     *
     * @param command The command to add.
     * @param label The label of the command.
     */
    void addCommand(Command<T, S> command, String label);

    /**
     * Removes a command from the platform.
     *
     * @param label The label of the command to remove.
     * @param subcommand true if the command is a subcommand, false otherwise.
     */
    void removeCommand(String label, boolean subcommand);
}
