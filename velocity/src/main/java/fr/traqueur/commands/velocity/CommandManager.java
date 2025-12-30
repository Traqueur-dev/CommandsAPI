package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.logging.Logger;

/**
 * This implementation of {@link fr.traqueur.commands.api.CommandManager} is used to provide the command manager in Spigot context.
 *
 * @param <T> The type of the plugin, must extend JavaPlugin.
 */
public class CommandManager<T> extends fr.traqueur.commands.api.CommandManager<T, CommandSource> {

    /**
     * Constructor for the CommandManager.
     *
     * @param instance The plugin instance associated with this command manager.
     * @param server   The server instance associated with this command manager.
     * @param logger   The logger instance associated with this command manager.
     */
    public CommandManager(T instance, ProxyServer server, Logger logger) {
        super(new VelocityPlatform<>(instance, server, logger));
    }
}
