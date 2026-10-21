package fr.traqueur.commands.paper;

import fr.traqueur.commands.adventure.ComponentMessageFormatter;
import fr.traqueur.commands.adventure.ComponentMessages;
import fr.traqueur.commands.spigot.arguments.OfflinePlayerArgument;
import fr.traqueur.commands.spigot.arguments.PlayerArgument;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The command manager of the Paper platform, which registers commands through Paper's
 * Brigadier API instead of the Bukkit command map.
 * <p>
 * Create it in {@code onEnable} and register the commands there: Paper builds its command tree
 * after the plugins are enabled, and a label registered later only answers after the next
 * {@code /minecraft:reload}. See {@link PaperPlatform} for what happens to a command added or
 * removed afterwards.
 * </p>
 *
 * @param <T> The type of the plugin.
 * @since 5.6.0
 */
public class CommandManager<T extends JavaPlugin> extends fr.traqueur.commands.api.CommandManager<T, CommandSender> {

    /**
     * Create the command manager of a plugin.
     *
     * @param plugin The plugin.
     */
    public CommandManager(T plugin) {
        this(new PaperPlatform<>(plugin));
    }

    /**
     * Create the command manager on a given platform.
     *
     * @param platform The platform.
     */
    protected CommandManager(PaperPlatform<T> platform) {
        super(platform);
        this.registerConverter(Player.class, new PlayerArgument());
        this.registerConverter(OfflinePlayer.class, new OfflinePlayerArgument());
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
     */
    public void setComponentFormatter(ComponentMessageFormatter<CommandSender> formatter) {
        ComponentMessages.install(this, formatter);
    }

    /**
     * Get the component formatter of the command manager.
     *
     * @return The formatter, or null when messages are sent as text.
     */
    public ComponentMessageFormatter<CommandSender> getComponentFormatter() {
        return ComponentMessages.formatterOf(this);
    }
}
