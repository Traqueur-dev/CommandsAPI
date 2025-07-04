package fr.traqueur.commands.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Argument used to parse an {@link OfflinePlayer} from a string.
 */
public class OfflinePlayerArgument implements ArgumentConverter<OfflinePlayer>, TabCompleter {

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses {@link Bukkit#getOfflinePlayer(String)} to parse the {@link OfflinePlayer}.
     */
    @Override
    public OfflinePlayer apply(String input) {
        return input != null ? Bukkit.getOfflinePlayer(input) : null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns a list of all player names.
     */
    @Override
    public List<String> onCompletion(CommandSender sender, List<String> args) {
        return Arrays.stream(Bukkit.getServer().getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList());
    }
}
