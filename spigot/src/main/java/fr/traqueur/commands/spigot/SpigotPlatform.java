package fr.traqueur.commands.spigot;

import fr.traqueur.commands.api.Command;
import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.CommandPlatform;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SpigotPlatform<T extends JavaPlugin> implements CommandPlatform<T> {

    private final T plugin;

    /**
     * The command map of the server.
     */
    private CommandMap commandMap;

    private CommandManager<T, CommandSender> commandManager;

    /**
     * The executor of the command manager.
     */
    private Executor<T> executor;

    /**
     * The constructor of the plugin command.
     */
    private Constructor<? extends PluginCommand> pluginConstructor;

    public SpigotPlatform(T plugin) {
        this.plugin = plugin;
        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            pluginConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginConstructor.setAccessible(true);
        } catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException e) {
            this.getLogger().severe("Unable to get the command map.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }



    @Override
    public T getPlugin() {
        return this.plugin;
    }

    @Override
    public void injectManager(CommandManager<T, ?> commandManager) {
        //noinspection unchecked
        this.commandManager = (CommandManager<T, CommandSender>)  commandManager;
        this.executor = new Executor<>(plugin, this.commandManager);
    }

    @Override
    public Logger getLogger() {
        return this.plugin.getLogger();
    }

    @Override
    public boolean hasPermission(Object sender, String permission) {
        if (sender instanceof org.bukkit.command.CommandSender) {
            org.bukkit.command.CommandSender commandSender = (org.bukkit.command.CommandSender) sender;
            return commandSender.hasPermission(permission);
        }
        return false;
    }

    @Override
    public void addCommand(Command<T, ?> command, String label) {
        String[] labelParts = label.split("\\.");
        String cmdLabel = labelParts[0].toLowerCase();
        AtomicReference<String> originCmdLabelRef = new AtomicReference<>(cmdLabel);
        int labelSize = labelParts.length;

        if(labelSize > 1) {
            this.commandManager.getCommands().values().stream()
                    .filter(commandInner -> !commandInner.isSubCommand())
                    .filter(commandInner -> commandInner.getAliases().contains(cmdLabel))
                    .findAny()
                    .ifPresent(commandInner -> originCmdLabelRef.set(commandInner.getName()));
        } else {
            originCmdLabelRef.set(label);
        }
        String originCmdLabel = originCmdLabelRef.get();

        if (commandMap.getCommand(originCmdLabel) == null) {
            PluginCommand cmd;
            try {
                cmd = pluginConstructor.newInstance(originCmdLabel, this.plugin);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            cmd.setExecutor(this.executor);
            cmd.setTabCompleter(this.executor);
            cmd.setAliases(command.getAliases().stream().map(s -> s.split("\\.")[0]).collect(Collectors.toList()));

            if(!commandMap.register(originCmdLabel, this.plugin.getName(), cmd)) {
                this.getLogger().severe("Unable to add the command " + originCmdLabel);
                return;
            }
        }

        if (!command.getDescription().equalsIgnoreCase("") && labelParts.length == 1) {
            Objects.requireNonNull(commandMap.getCommand(originCmdLabel)).setDescription(command.getDescription());
        }

        if (!command.getUsage().equalsIgnoreCase("") && labelParts.length == 1) {
            Objects.requireNonNull(commandMap.getCommand(originCmdLabel)).setUsage(command.getUsage());
        }
    }

    @Override
    public void removeCommand(String label, boolean subcommand) {
        if(subcommand && this.commandMap.getCommand(label) != null) {
            Objects.requireNonNull(this.commandMap.getCommand(label)).unregister(commandMap);
        }
    }
}
