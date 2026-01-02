package fr.traqueur.commands.spigot;

import fr.traqueur.commands.api.resolver.SenderResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Sender resolver for the Bukkit/Spigot platform.
 * 
 * <p>Resolves method parameter types to appropriate sender objects:</p>
 * <ul>
 *   <li>{@link CommandSender} → the raw sender (any)</li>
 *   <li>{@link Player} → cast to Player (requires gameOnly)</li>
 *   <li>{@link ConsoleCommandSender} → cast to Console</li>
 * </ul>
 * 
 * @since 5.0.0
 */
public class SpigotSenderResolver implements SenderResolver<CommandSender> {

    /**
     * Creates a new Bukkit sender resolver.
     */
    public SpigotSenderResolver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canResolve(Class<?> type) {
        return CommandSender.class.isAssignableFrom(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object resolve(CommandSender sender, Class<?> type) {
        if (type.isInstance(sender)) {
            return type.cast(sender);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGameOnly(Class<?> type) {
        return Player.class.isAssignableFrom(type);
    }
}