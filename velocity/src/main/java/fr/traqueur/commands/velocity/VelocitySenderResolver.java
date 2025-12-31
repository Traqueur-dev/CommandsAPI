package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.traqueur.commands.api.resolver.SenderResolver;

/**
 * Sender resolver for the Velocity platform.
 * 
 * <p>Resolves method parameter types to appropriate sender objects:</p>
 * <ul>
 *   <li>{@link CommandSource} → the raw sender (any)</li>
 *   <li>{@link Player} → cast to Player (requires gameOnly)</li>
 *   <li>{@link ConsoleCommandSource} → cast to Console</li>
 * </ul>
 * 
 * @since 5.0.0
 */
public class VelocitySenderResolver implements SenderResolver<CommandSource> {

    /**
     * Creates a new Velocity sender resolver.
     */
    public VelocitySenderResolver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canResolve(Class<?> type) {
        return CommandSource.class.isAssignableFrom(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object resolve(CommandSource sender, Class<?> type) {
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