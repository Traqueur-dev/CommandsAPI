package fr.traqueur.commands.spigot.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Argument converter for {@link Player}.
 */
public class PlayerArgument implements ArgumentConverter<Player>, fr.traqueur.commands.api.arguments.TabCompleter<CommandSender> {

    /**
     * Creates a new PlayerArgument.
     */
    public PlayerArgument() {}

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses {@link Bukkit#getPlayer(String)} to convert the input to a player.
     */
    @Override
    public Player apply(String input) {
        return input != null ? Bukkit.getPlayer(input) : null;
    }

    /**
     * {@inheritDoc}
     * This implementation returns a list of all online player names.
     */
    @Override
    public List<String> onCompletion(CommandSender sender, List<String> args) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}