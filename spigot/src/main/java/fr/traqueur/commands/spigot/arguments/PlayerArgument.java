package fr.traqueur.commands.spigot.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Argument converter for {@link Player}.
 */
public class PlayerArgument implements ArgumentConverter<Player>, TabCompleter<CommandSender> {

    /**
     * Cache TTL in milliseconds.
     */
    private static final long CACHE_TTL_MS = 1000;

    /**
     * Cached player names.
     */
    private volatile List<String> cachedNames = Collections.emptyList();

    /**
     * Last cache update time.
     */
    private volatile long cacheTime = 0;

    /**
     * Creates a new PlayerArgument.
     */
    public PlayerArgument() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public Player apply(String input) {
        return input != null ? Bukkit.getPlayer(input) : null;
    }

    /**
     * {@inheritDoc}
     * Returns cached list of online player names, refreshed every second.
     */
    @Override
    public List<String> onCompletion(CommandSender sender, List<String> args) {
        long now = System.currentTimeMillis();
        if (now - cacheTime > CACHE_TTL_MS) {
            cachedNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            cacheTime = now;
        }
        return cachedNames;
    }
}