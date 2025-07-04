package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.traqueur.commands.api.Command;
import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.CommandPlatform;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * The Spigot implementation of the CommandPlatform interface.
 * This class handles the registration and management of commands in a Spigot environment.
 *
 * @param <T> The type of the JavaPlugin that this platform is associated with.
 */
public class VelocityPlatform<T> implements CommandPlatform<T, CommandSource> {

    /**
     * The plugin instance associated with this platform.
     * This is used to access the plugin's methods and properties.
     */
    private final T plugin;

    /**
     * The server instance associated with this platform.
     * This is used to access the server's methods and properties.
     */
    private final ProxyServer server;

    /**
     * The logger instance associated with this platform.
     * This is used to log messages to the server's console.
     */
    private final Logger logger;

    /**
     * The command manager instance associated with this platform.
     * This is used to manage commands and their execution.
     */
    private CommandManager<T, CommandSource> commandManager;

    /**
     * Constructor for the VelocityPlatform.
     *
     * @param plugin The plugin instance associated with this platform.
     * @param server The server instance associated with this platform.
     * @param logger The logger instance associated with this platform.
     */
    public VelocityPlatform(T plugin, ProxyServer server, Logger logger) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getPlugin() {
        return this.plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void injectManager(CommandManager<T, CommandSource> commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPermission(CommandSource sender, String permission) {
        return sender.hasPermission(permission);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCommand(Command<T, CommandSource> command, String label) {
        String[] labelParts = label.split("\\.");
        String cmdLabel = labelParts[0].toLowerCase();
        AtomicReference<String> originCmdLabelRef = new AtomicReference<>(cmdLabel);

        if (labelParts.length > 1) {
            this.commandManager.getCommands().values().stream()
                    .filter(commandInner -> !commandInner.isSubCommand())
                    .filter(commandInner -> commandInner.getAliases().contains(cmdLabel))
                    .findAny()
                    .ifPresent(commandInner -> originCmdLabelRef.set(commandInner.getName()));
        } else {
            originCmdLabelRef.set(label);
        }

        String originCmdLabel = originCmdLabelRef.get().toLowerCase();
        com.velocitypowered.api.command.CommandManager velocityCmdManager = server.getCommandManager();

        if (velocityCmdManager.getCommandMeta(originCmdLabel) == null) {
            String[] aliases = command.getAliases().stream()
                    .map(a -> a.split("\\.")[0].toLowerCase())
                    .distinct()
                    .filter(a -> !a.equalsIgnoreCase(originCmdLabel))
                    .toArray(String[]::new);

            velocityCmdManager.register(
                    velocityCmdManager.metaBuilder(originCmdLabel)
                            .aliases(aliases)
                            .plugin(plugin)
                            .build(),
                    new Executor<>(commandManager)
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeCommand(String label, boolean subcommand) {
        if(subcommand && this.server.getCommandManager().getCommandMeta(label) != null) {
            this.server.getCommandManager().unregister(this.server.getCommandManager().getCommandMeta(label));
        } else {
            this.server.getCommandManager().unregister(label);
        }
    }
}
