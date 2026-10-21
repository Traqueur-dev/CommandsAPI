package fr.traqueur.commands.paper;

import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandPlatform;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.api.resolver.SenderResolver;
import fr.traqueur.commands.api.utils.Patterns;
import fr.traqueur.commands.spigot.SpigotSenderResolver;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The Paper implementation of the CommandPlatform interface, on Paper's Brigadier API.
 * <p>
 * Commands are not put in the Bukkit command map: they are handed to Paper as Brigadier nodes,
 * through {@link LifecycleEvents#COMMANDS}, so that the client knows the shape of their
 * arguments (see {@link BrigadierCommandTree}).
 * </p>
 * <p>
 * <b>When the nodes are built.</b> Paper asks for the commands when it builds its command tree:
 * once at startup, after the plugins are enabled, then on every {@code /minecraft:reload}. The
 * nodes are built at that moment, from the commands the manager holds then. So:
 * </p>
 * <ul>
 *     <li>a command registered in {@code onEnable} (or in the plugin's bootstrap) is there at startup;</li>
 *     <li>a command whose label is new, registered after startup, gets no node until the next
 *     {@code /minecraft:reload}: Paper offers no way to add one in between. A warning says so;</li>
 *     <li>a subcommand or an argument added afterwards to a label that already has a node is not in
 *     that node either, until the next reload;</li>
 *     <li>a command unregistered after startup keeps its node, but the node's {@code requires} reads
 *     the manager, so it is hidden from the clients (their command list is refreshed) and it can no
 *     longer be run: it answers as an unknown command. Registering it again shows it again, with the
 *     arguments it had when the node was built.</li>
 * </ul>
 *
 * @param <T> The type of the plugin.
 * @since 5.6.0
 */
public class PaperPlatform<T extends JavaPlugin> implements CommandPlatform<T, CommandSender> {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final T plugin;

    /**
     * The root labels handed to Paper the last time it asked for the commands.
     */
    private final Set<String> registered = new HashSet<>();

    private CommandManager<T, CommandSender> commandManager;

    /**
     * Whether Paper has asked for the commands at least once.
     */
    private boolean built;

    /**
     * Whether a refresh of the clients' command lists is already scheduled.
     */
    private boolean refreshScheduled;

    /**
     * Create the platform and ask Paper for the commands event.
     * <p>
     * Create it in {@code onEnable}, or earlier: the event handler must be registered before Paper
     * builds its command tree.
     * </p>
     *
     * @param plugin The plugin.
     */
    public PaperPlatform(T plugin) {
        this(plugin, true);
    }

    /**
     * Create the platform, optionally without registering the event handler (for tests).
     *
     * @param plugin   The plugin.
     * @param listen   Whether to register the commands event handler.
     */
    PaperPlatform(T plugin, boolean listen) {
        this.plugin = plugin;
        if (listen) {
            plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                    event -> this.registerAll(event.registrar()));
        }
    }

    /**
     * Hand every root label of the manager to Paper.
     *
     * @param registrar The registrar of the commands event.
     */
    void registerAll(Commands registrar) {
        this.built = true;
        this.registered.clear();
        BrigadierCommandTree<T, CommandSender, CommandSourceStack> tree =
                new BrigadierCommandTree<>(this.commandManager, CommandSourceStack::getSender);

        for (String label : tree.roots()) {
            LiteralCommandNode<CommandSourceStack> node = tree.build(label);
            if (node == null) {
                continue;
            }
            Set<String> labels = registrar.register(node, this.descriptionOf(label), List.of());
            this.registered.add(label);
            if (!labels.contains(label)) {
                this.getLogger().warning("Command '" + label + "' could not take its plain label, which another command holds:"
                        + " '/" + this.plugin.getName().toLowerCase(Locale.ROOT) + ":" + label + "' answers instead.");
            }
        }
    }

    private String descriptionOf(String label) {
        CommandTree.CommandNode<T, CommandSender> node = this.commandManager.getCommands().getRoot().getChildren().get(label);
        if (node == null) {
            return null;
        }
        return node.getCommand()
                .map(Command::getDescription)
                .filter(description -> !description.isEmpty())
                .orElse(null);
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
        this.commandManager = commandManager;
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
        return sender != null && sender.hasPermission(permission);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    /**
     * Sends a text message, with its {@code &} or {@code §} colour codes, as on Spigot.
     */
    @Override
    @SuppressWarnings("deprecation")
    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(LEGACY.deserialize(org.bukkit.ChatColor.translateAlternateColorCodes('&', message)));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Nothing is sent to Paper here: the nodes are built when Paper asks for them. A new label
     * registered once that happened is logged, since it will only answer after the next reload.
     * </p>
     */
    @Override
    public void addCommand(Command<T, CommandSender> command, String label) {
        String root = Patterns.DOT.split(label)[0].toLowerCase(Locale.ROOT);
        if (this.built && !this.registered.contains(root)) {
            this.getLogger().warning("Command '" + root + "' was registered after Paper built its command tree:"
                    + " it will answer after the next /minecraft:reload. Register commands in onEnable.");
            return;
        }
        this.refreshClients();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The node stays in Paper's tree, hidden by its {@code requires}: the clients' command lists are
     * refreshed so that it disappears.
     * </p>
     */
    @Override
    public void removeCommand(String label, boolean subcommand) {
        this.refreshClients();
    }

    /**
     * Resend the command tree to the online players, so that what they see follows the manager.
     */
    private void refreshClients() {
        if (!this.built || this.refreshScheduled) {
            return;
        }
        try {
            if (this.plugin.isEnabled()) {
                // Once per tick, however many labels changed: unregisterAll changes them all at once.
                this.refreshScheduled = true;
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    this.refreshScheduled = false;
                    Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
                });
            } else {
                // A disabled plugin cannot schedule: refresh now.
                Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
            }
        } catch (RuntimeException e) {
            this.refreshScheduled = false;
            // No server (a test, or a shutdown in progress): nothing to refresh.
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SenderResolver<CommandSender> getSenderResolver() {
        return new SpigotSenderResolver();
    }
}
