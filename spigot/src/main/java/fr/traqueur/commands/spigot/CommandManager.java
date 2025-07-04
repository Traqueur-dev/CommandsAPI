package fr.traqueur.commands.spigot;

import fr.traqueur.commands.spigot.arguments.OfflinePlayerArgument;
import fr.traqueur.commands.spigot.arguments.PlayerArgument;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * {@inheritDoc}
 *
 * @param <T> The type of the plugin, must extend JavaPlugin.
 * <p>
 * This implementation of {@link fr.traqueur.commands.api.CommandManager} is used to provide the command manager in Spigot context.
 * </p>
 */
public class CommandManager<T extends JavaPlugin> extends fr.traqueur.commands.api.CommandManager<T, CommandSender> {

    public CommandManager(T plugin) {
        super(new SpigotPlatform<>(plugin));
        this.registerConverter(Player.class, new PlayerArgument());
        this.registerConverter(OfflinePlayer.class, new OfflinePlayerArgument());
    }
}
