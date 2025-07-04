package fr.traqueur.commands.spigot;

import fr.traqueur.commands.spigot.arguments.OfflinePlayerArgument;
import fr.traqueur.commands.spigot.arguments.PlayerArgument;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This implementation of {@link fr.traqueur.commands.api.CommandManager} is used to provide the command manager in Spigot context.
 * @param <T> The type of the plugin, must extend JavaPlugin.
 */
public class CommandManager<T extends JavaPlugin> extends fr.traqueur.commands.api.CommandManager<T, CommandSender> {

    /**
     * Constructor for the CommandManager.
     *
     * @param plugin The plugin instance associated with this command manager.
     */
    public CommandManager(T plugin) {
        super(new SpigotPlatform<>(plugin));
        this.registerConverter(Player.class, new PlayerArgument());
        this.registerConverter(OfflinePlayer.class, new OfflinePlayerArgument());
    }
}
