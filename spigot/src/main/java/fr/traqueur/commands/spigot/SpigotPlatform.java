package fr.traqueur.commands.spigot;

import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.models.CommandPlatform;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

/**
 * The Spigot implementation of the CommandPlatform interface.
 * This class handles the registration and management of commands in a Spigot environment.
 *
 * @param <T> The type of the JavaPlugin that this platform is associated with.
 */
public class SpigotPlatform<T extends JavaPlugin> implements CommandPlatform<T, CommandSender> {

    /**
     * The plugin instance associated with this platform.
     * This is used to access the plugin's methods and properties.
     */
    private final T plugin;

    /**
     * The command map of the server.
     */
    private CommandMap commandMap;

    /**
     * The command manager of the plugin.
     * This is used to manage commands and their execution.
     */
    private CommandManager<T, CommandSender> commandManager;

    /**
     * The executor of the command manager.
     */
    private SpigotExecutor<T> spigotExecutor;

    /**
     * The constructor of the plugin command.
     */
    private Constructor<? extends PluginCommand> pluginConstructor;

    /**
     * Constructor for the SpigotPlatform.
     * Initializes the command map and plugin command constructor.
     *
     * @param plugin The JavaPlugin instance associated with this platform.
     */
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
    public void injectManager(CommandManager<T, CommandSender> commandManager) {
        //noinspection unchecked
        this.commandManager = commandManager;
        this.spigotExecutor = new SpigotExecutor<>(plugin, this.commandManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Logger getLogger() {
        return this.plugin.getLogger();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPermission(CommandSender sender, String permission) {
        if (sender != null) {
            return sender.hasPermission(permission);
        }
        return false;
    }

    @Override
    public boolean isPlayer(CommandSender sender) {
        return sender instanceof org.bukkit.entity.Player;
    }

    @Override
    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCommand(Command<T, CommandSender> command, String label) {
        String[] labelParts = label.split("\\.");
        String cmdLabel = labelParts[0].toLowerCase();

        boolean alreadyInTree = commandManager.getCommands()
                .getRoot()
                .getChildren()
                .containsKey(cmdLabel);
        boolean alreadyInMap = commandMap.getCommand(cmdLabel) != null;

        if (!alreadyInTree && !alreadyInMap) {
            try {
                PluginCommand cmd = pluginConstructor.newInstance(cmdLabel, plugin);
                cmd.setExecutor(spigotExecutor);
                cmd.setTabCompleter(spigotExecutor);
                cmd.setAliases(
                        command.getAliases().stream()
                                .map(a -> a.split("\\.")[0])
                                .filter(a -> !a.equalsIgnoreCase(cmdLabel))
                                .distinct()
                                .collect(Collectors.toList())
                );

                if (!commandMap.register(cmdLabel, plugin.getName(), cmd)) {
                    getLogger().severe("Unable to add command " + cmdLabel);
                    return;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (!command.getDescription().equalsIgnoreCase("") && labelParts.length == 1) {
            Objects.requireNonNull(commandMap.getCommand(cmdLabel)).setDescription(command.getDescription());
        }

        if (!command.getUsage().equalsIgnoreCase("") && labelParts.length == 1) {
            Objects.requireNonNull(commandMap.getCommand(cmdLabel)).setUsage(command.getUsage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeCommand(String label, boolean subcommand) {
        if(subcommand && this.commandMap.getCommand(label) != null) {
            Objects.requireNonNull(this.commandMap.getCommand(label)).unregister(commandMap);
        }
    }
}
