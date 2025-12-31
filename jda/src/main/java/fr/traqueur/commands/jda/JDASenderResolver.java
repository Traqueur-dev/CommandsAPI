package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.resolver.SenderResolver;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Sender resolver for the JDA (Discord) platform.
 * 
 * <p>Resolves method parameter types to appropriate objects:</p>
 * <ul>
 *   <li>{@link SlashCommandInteractionEvent} → the raw event</li>
 *   <li>{@link User} → event.getUser()</li>
 *   <li>{@link Member} → event.getMember() (requires guild, gameOnly = true)</li>
 *   <li>{@link MessageChannelUnion} → event.getChannel()</li>
 * </ul>
 * 
 * @since 5.0.0
 */
public class JDASenderResolver implements SenderResolver<SlashCommandInteractionEvent> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canResolve(Class<?> type) {
        return SlashCommandInteractionEvent.class.isAssignableFrom(type)
                || User.class.isAssignableFrom(type)
                || Member.class.isAssignableFrom(type)
                || MessageChannelUnion.class.isAssignableFrom(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object resolve(SlashCommandInteractionEvent event, Class<?> type) {
        if (SlashCommandInteractionEvent.class.isAssignableFrom(type)) {
            return event;
        }
        if (User.class.isAssignableFrom(type)) {
            return event.getUser();
        }
        if (Member.class.isAssignableFrom(type)) {
            return event.getMember(); // null if not in guild
        }
        if (MessageChannelUnion.class.isAssignableFrom(type)) {
            return event.getChannel();
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