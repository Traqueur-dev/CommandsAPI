package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.resolver.SenderResolver;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

/**
 * Sender resolver for the JDA (Discord) platform.
 *
 * <p>Resolves method parameter types to appropriate objects:</p>
 * <ul>
 *   <li>{@link JDAInteractionContext} → the interaction context</li>
 *   <li>{@link User} → context.getUser()</li>
 *   <li>{@link Member} → context.getMember() (requires guild, gameOnly = true)</li>
 *   <li>{@link MessageChannelUnion} → context.getChannel()</li>
 * </ul>
 *
 * @since 5.0.0
 */
public class JDASenderResolver implements SenderResolver<JDAInteractionContext> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canResolve(Class<?> type) {
        return JDAInteractionContext.class.isAssignableFrom(type)
                || User.class.isAssignableFrom(type)
                || Member.class.isAssignableFrom(type)
                || MessageChannelUnion.class.isAssignableFrom(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object resolve(JDAInteractionContext context, Class<?> type) {
        if (JDAInteractionContext.class.isAssignableFrom(type)) {
            return context;
        }
        if (User.class.isAssignableFrom(type)) {
            return context.getUser();
        }
        if (Member.class.isAssignableFrom(type)) {
            return context.getMember(); // null if not in guild
        }
        if (MessageChannelUnion.class.isAssignableFrom(type)) {
            return context.getChannel();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGameOnly(Class<?> type) {
        // Member requires guild context (like Player requires game context)
        return Member.class.isAssignableFrom(type);
    }
}
