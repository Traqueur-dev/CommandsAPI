package fr.traqueur.commands.spigot;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.exceptions.CommandRegistrationException;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandPlatform;
import fr.traqueur.commands.api.resolver.SenderResolver;
import fr.traqueur.commands.api.utils.Patterns;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Objects;
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
        } catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchFieldException |
                 NoSuchMethodException e) {
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
        String[] labelParts = Patterns.DOT.split(label);
        String cmdLabel = labelParts[0].toLowerCase();

        boolean alreadyInTree = commandManager.getCommands()
                .getRoot()
                .getChildren()
                .containsKey(cmdLabel);
        org.bukkit.command.Command existing = commandMap.getCommand(cmdLabel);
        boolean alreadyInMap = existing != null;
        // An alias we registered ourselves comes back through here: nothing to take over.
        boolean alreadyOurs = existing instanceof PluginCommand pluginCommand
                && pluginCommand.getExecutor() == spigotExecutor;

        if (!alreadyInTree && alreadyInMap && !alreadyOurs) {
            if (command.isOverride()) {
                alreadyInMap = !this.freeLabel(cmdLabel);
            } else {
                String owner = existing instanceof PluginCommand ownerCommand
                        ? "the plugin " + ownerCommand.getPlugin().getName()
                        : "the server";
                getLogger().warning("Command '" + cmdLabel + "' is already registered by " + owner
                        + " and was not bound: that command answers instead."
                        + " Mark it as an override to take the label over.");
            }
        }

        if (!alreadyInTree && !alreadyInMap) {
            try {
                PluginCommand cmd = pluginConstructor.newInstance(cmdLabel, plugin);
                cmd.setExecutor(spigotExecutor);
                cmd.setTabCompleter(spigotExecutor);
                cmd.setAliases(
                        command.getAliases().stream()
                                .map(a -> Patterns.DOT.split(a)[0])
                                .filter(a -> !a.equalsIgnoreCase(cmdLabel))
                                .distinct()
                                .collect(Collectors.toList())
                );

                if (!commandMap.register(cmdLabel, plugin.getName(), cmd)) {
                    getLogger().severe("Unable to add command " + cmdLabel);
                    return;
                }
            } catch (Exception e) {
                throw new CommandRegistrationException("Failed to register command '" + cmdLabel + "' in Spigot", e);
            }
        }

        // Only decorate our own registration: on a label we did not take over, the command sitting in
        // the map belongs to the server or to another plugin.
        if (labelParts.length == 1
                && commandMap.getCommand(cmdLabel) instanceof PluginCommand registered
                && registered.getPlugin().equals(plugin)) {
            if (!command.getDescription().isEmpty()) {
                registered.setDescription(command.getDescription());
            }
            if (!command.getUsage().isEmpty()) {
                registered.setUsage(command.getUsage());
            }
        }
    }

    /**
     * Removes the command currently holding a label so that it can be registered again.
     *
     * <p>Only the plain label is dropped: the namespaced entry ({@code minecraft:gamemode},
     * {@code otherplugin:home}) is left in place, so the overridden command stays reachable — this is
     * how a plugin command shadows a vanilla one on Spigot.</p>
     *
     * @param label The label to free.
     * @return {@code true} if the label is now free.
     */
    private boolean freeLabel(String label) {
        org.bukkit.command.Command existing = commandMap.getCommand(label);
        if (existing == null) {
            return true;
        }
        if (!(commandMap instanceof SimpleCommandMap simpleCommandMap)) {
            getLogger().warning("Cannot override command '" + label + "': the server command map is a "
                    + commandMap.getClass().getName() + ", which does not expose its known commands.");
            return false;
        }
        simpleCommandMap.getKnownCommands().remove(label);
        existing.unregister(commandMap);
        return commandMap.getCommand(label) == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeCommand(String label, boolean subcommand) {
        if (subcommand && this.commandMap.getCommand(label) != null) {
            Objects.requireNonNull(this.commandMap.getCommand(label)).unregister(commandMap);
        }
    }

    @Override
    public SenderResolver<CommandSender> getSenderResolver() {
        return new SpigotSenderResolver();
    }
}
