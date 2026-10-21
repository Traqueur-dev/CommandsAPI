package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.traqueur.commands.adventure.ComponentMessageFormatter;
import fr.traqueur.commands.adventure.ComponentMessages;

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

    /**
     * Set the component formatter of the command manager.
     * <p>
     * Every message of the library is then built as a {@link net.kyori.adventure.text.Component}
     * by the formatter and sent as such, click and hover events included. The text
     * {@link #setMessageFormatter message formatter} is no longer called, except for the
     * messages the component formatter declines by returning null.
     * </p>
     *
     * @param formatter The formatter, or null to send messages as text again.
     * @since 5.5.0
     */
    public void setComponentFormatter(ComponentMessageFormatter<CommandSource> formatter) {
        ComponentMessages.install(this, formatter);
    }

    /**
     * Get the component formatter of the command manager.
     *
     * @return The formatter, or null when messages are sent as text.
     * @since 5.5.0
     */
    public ComponentMessageFormatter<CommandSource> getComponentFormatter() {
        return ComponentMessages.formatterOf(this);
    }
}
