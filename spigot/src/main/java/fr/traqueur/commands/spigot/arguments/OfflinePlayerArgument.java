package fr.traqueur.commands.spigot.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Argument used to parse an {@link OfflinePlayer} from a string.
 */
public class OfflinePlayerArgument implements ArgumentConverter<OfflinePlayer>, TabCompleter<CommandSender> {

    /**
     * Cache TTL in milliseconds (longer for offline players as it's more expensive).
     */
    private static final long CACHE_TTL_MS = 5000;

    /**
     * Cached offline player names.
     */
    private volatile List<String> cachedNames = Collections.emptyList();

    /**
     * Last cache update time.
     */
    private volatile long cacheTime = 0;

    /**
     * Creates a new OfflinePlayerArgument.
     */
    public OfflinePlayerArgument() {}

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override
    public OfflinePlayer apply(String input) {
        return input != null ? Bukkit.getOfflinePlayer(input) : null;
    }

    /**
     * {@inheritDoc}
     * Returns cached list of offline player names, refreshed every 5 seconds.
     */
    @Override
    public List<String> onCompletion(CommandSender sender, List<String> args) {
        long now = System.currentTimeMillis();
        if (now - cacheTime > CACHE_TTL_MS) {
            cachedNames = Arrays.stream(Bukkit.getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            cacheTime = now;
        }
        return cachedNames;
    }
}