package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.traqueur.commands.api.Command;
import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.CommandPlatform;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class VelocityPlatform<T> implements CommandPlatform<T, CommandSource> {

    private final T plugin;
    private final ProxyServer server;
    private final Logger logger;

    private CommandManager<T, CommandSource> commandManager;

    public VelocityPlatform(T plugin, ProxyServer server, Logger logger) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
    }

    @Override
    public T getPlugin() {
        return this.plugin;
    }

    @Override
    public void injectManager(CommandManager<T, CommandSource> commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public boolean hasPermission(CommandSource sender, String permission) {
        return sender.hasPermission(permission);
    }

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
                            .build(),
                    new Executor<>(commandManager)
            );
        }
    }

    @Override
    public void removeCommand(String label, boolean subcommand) {
        if(subcommand && this.server.getCommandManager().getCommandMeta(label) != null) {
            this.server.getCommandManager().unregister(this.server.getCommandManager().getCommandMeta(label));
        } else {
            this.server.getCommandManager().unregister(label);
        }
    }
}
