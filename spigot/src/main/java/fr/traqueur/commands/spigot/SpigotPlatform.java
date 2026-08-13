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
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

        boolean overriding = false;

        if (!alreadyInTree && alreadyInMap && !alreadyOurs) {
            if (command.isOverride()) {
                overriding = true;
                this.releaseLabel(cmdLabel);
                // The command map has the last word, not the release attempt: a server can hand the
                // label over on its own (Paper lets any vanilla command be overridden), and the
                // registration is the only thing that says whether the takeover actually happened.
                // Refusing to even try, on the sole ground that the holder did not step aside, is
                // how an override ended up doing nothing at all on Paper.
                alreadyInMap = false;
            } else {
                getLogger().warning("Command '" + cmdLabel + "' is already registered by " + this.ownerOf(existing)
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
                    if (overriding) {
                        // Not a failed registration: the command is bound, under its namespaced form
                        // only (the command map relabels what it refuses). Saying which name answers
                        // is the difference between a bug report and a permission to fix.
                        getLogger().warning("Cannot override command '" + cmdLabel + "': the label is still held by "
                                + this.ownerOf(commandMap.getCommand(cmdLabel)) + "."
                                + " '/" + plugin.getName().toLowerCase(Locale.ENGLISH) + ":" + cmdLabel + "' answers instead.");
                    } else {
                        getLogger().severe("Unable to add command " + cmdLabel);
                    }
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
     * Makes the command currently holding a label give it up, so that a new registration can claim it.
     *
     * <p>The command map refuses a label whose holder still claims it as its own
     * ({@code conflict.getLabel().equals(label)}), and only lets a command be relabelled while it is
     * detached. So the holder is unregistered, then relabelled to its namespaced form: the plain
     * label is up for grabs and the next {@code register} overwrites it, while the namespaced entry
     * ({@code minecraft:gamemode}, {@code otherplugin:home}) keeps pointing at it — this is how a
     * plugin command shadows a vanilla one on Spigot.</p>
     *
     * <p>Best effort, and deliberately silent: on a Brigadier-backed server the map is a live view
     * over the dispatcher, and a label held by something that is not a Bukkit command (a vanilla
     * command, or one registered through the modern API) is wrapped into a NEW object on every
     * lookup — so both calls below land on a throwaway and change nothing. That is not a failure:
     * those same servers let the registration overwrite the holder by itself. The label is still
     * released here for the case the map cannot handle alone, another plugin's legacy command, and
     * what actually happened is read from the registration, not from here.</p>
     *
     * @param label The label to release.
     */
    private void releaseLabel(String label) {
        org.bukkit.command.Command existing = commandMap.getCommand(label);
        if (existing == null) {
            return;
        }

        String namespace = this.namespaceOf(existing, label);
        existing.unregister(commandMap);
        existing.setLabel(namespace + ":" + label);
    }

    /**
     * Names who currently answers on a label, for a log line the reader can act on.
     *
     * <p>Identification goes through {@link PluginIdentifiableCommand} rather than
     * {@link PluginCommand}: a command registered through a modern Brigadier API is not a
     * {@code PluginCommand}, but it does carry its plugin — reporting it as "the server" would send
     * the reader looking for a conflict on the wrong side.</p>
     *
     * @param command The command holding the label, may be {@code null}.
     * @return A human readable owner.
     */
    private String ownerOf(org.bukkit.command.Command command) {
        if (command instanceof PluginIdentifiableCommand identifiable) {
            return "the plugin " + identifiable.getPlugin().getName();
        }
        return "the server";
    }

    /**
     * Finds the namespace a command is registered under, by probing the command map for the
     * namespaced entry that points back at it.
     *
     * @param command The registered command.
     * @param label   The label it is registered under.
     * @return The namespace, or {@code overridden} when none could be confirmed.
     */
    private String namespaceOf(org.bukkit.command.Command command, String label) {
        List<String> candidates = new ArrayList<>();
        if (command instanceof PluginIdentifiableCommand identifiable) {
            candidates.add(identifiable.getPlugin().getName().toLowerCase(Locale.ENGLISH));
        }
        candidates.addAll(Arrays.asList("minecraft", "bukkit", "spigot", "paper"));

        for (String candidate : candidates) {
            if (commandMap.getCommand(candidate + ":" + label) == command) {
                return candidate;
            }
        }
        return "overridden";
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
