package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandPlatform;
import fr.traqueur.commands.api.resolver.SenderResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.logging.Logger;

/**
 * The Spigot implementation of the CommandPlatform interface.
 * This class handles the registration and management of commands in a Spigot environment.
 *
 * @param <T> The type of the JavaPlugin that this platform is associated with.
 */
public class VelocityPlatform<T> implements CommandPlatform<T, CommandSource> {


    /**
     * The serializer used to convert legacy components to Adventure components.
     */
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    /**
     * The MiniMessage instance used for parsing messages.
     */
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

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

    @Override
    public boolean isPlayer(CommandSource sender) {
        return sender instanceof com.velocitypowered.api.proxy.Player;
    }

    @Override
    public void sendMessage(CommandSource sender, String message) {
        sender.sendMessage(this.parse(message));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCommand(Command<T, CommandSource> command, String label) {
        String[] labelParts = label.split("\\.");
        String cmdLabel = labelParts[0].toLowerCase();

        com.velocitypowered.api.command.CommandManager velocityCmdManager = server.getCommandManager();

        boolean alreadyInTree = commandManager.getCommands()
                .getRoot()
                .getChildren()
                .containsKey(cmdLabel);
        boolean alreadyInMap = velocityCmdManager.getCommandMeta(cmdLabel) != null;

        if (!alreadyInTree && !alreadyInMap) {
            String[] aliases = command.getAliases().stream()
                    .map(a -> a.split("\\.")[0].toLowerCase())
                    .filter(a -> !a.equalsIgnoreCase(cmdLabel))
                    .distinct()
                    .toArray(String[]::new);

            velocityCmdManager.register(
                    velocityCmdManager.metaBuilder(cmdLabel)
                            .aliases(aliases)
                            .plugin(plugin)
                            .build(),
                    new VelocityExecutor<>(commandManager)
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeCommand(String label, boolean subcommand) {
        if (subcommand && this.server.getCommandManager().getCommandMeta(label) != null) {
            this.server.getCommandManager().unregister(this.server.getCommandManager().getCommandMeta(label));
        } else {
            this.server.getCommandManager().unregister(label);
        }
    }

    @Override
    public SenderResolver<CommandSource> getSenderResolver() {
        return new VelocitySenderResolver();
    }

    /**
     * Parses a message from legacy format to Adventure format.
     *
     * @param message The message in legacy format.
     * @return The parsed message in Adventure format.
     */
    private Component parse(String message) {
        Component legacy = SERIALIZER.deserialize(message);
        String asMini = MINI_MESSAGE.serialize(legacy);
        return MINI_MESSAGE.deserialize(asMini);
    }
}
