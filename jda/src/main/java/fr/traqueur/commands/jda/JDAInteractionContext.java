package fr.traqueur.commands.jda;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;

/**
 * Wrapper interface that provides common access to both SlashCommandInteractionEvent
 * and CommandAutoCompleteInteractionEvent for use in TabCompleters.
 */
public interface JDAInteractionContext {

    User getUser();
    Member getMember();
    Guild getGuild();
    MessageChannelUnion getChannel();
    boolean isFromGuild();

    /**
     * Get the underlying interaction.
     */
    Interaction getEvent();

    /**
     * Wrap a SlashCommandInteractionEvent.
     */
    static JDAInteractionContext wrap(SlashCommandInteractionEvent event) {
        return new JDAInteractionContext() {
            @Override
            public User getUser() { return event.getUser(); }

            @Override
            public Member getMember() { return event.getMember(); }

            @Override
            public Guild getGuild() { return event.getGuild(); }

            @Override
            public MessageChannelUnion getChannel() { return event.getChannel(); }

            @Override
            public boolean isFromGuild() { return event.isFromGuild(); }

            @Override
            public Interaction getEvent() { return event; }
        };
    }

    /**
     * Wrap a CommandAutoCompleteInteractionEvent.
     */
    static JDAInteractionContext wrap(CommandAutoCompleteInteractionEvent event) {
        return new JDAInteractionContext() {
            @Override
            public User getUser() { return event.getUser(); }

            @Override
            public Member getMember() { return event.getMember(); }

            @Override
            public Guild getGuild() { return event.getGuild(); }

            @Override
            public MessageChannelUnion getChannel() { return event.getChannel(); }

            @Override
            public boolean isFromGuild() { return event.isFromGuild(); }

            @Override
            public Interaction getEvent() { return event; }
        };
    }
}