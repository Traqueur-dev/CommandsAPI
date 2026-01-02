package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.logging.Logger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * JDA-specific implementation of Arguments that provides direct access to Discord entities.
 * This class extends the base Arguments class and adds methods to retrieve JDA-specific types
 * like User, Member, Role, and Channel directly from slash command options.
 */
public class JDAArguments extends Arguments {

    /**
     * The slash command interaction event that triggered this command.
     */
    private final SlashCommandInteractionEvent event;

    /**
     * Constructor for JDAArguments.
     *
     * @param logger The logger instance.
     * @param event  The slash command interaction event.
     */
    public JDAArguments(Logger logger, SlashCommandInteractionEvent event) {
        super(logger);
        this.event = event;
    }

    /**
     * Get the slash command interaction event.
     *
     * @return The slash command interaction event.
     */
    public SlashCommandInteractionEvent getEvent() {
        return event;
    }

    /**
     * Reply to the interaction.
     *
     * @param message The message to send.
     */
    public void reply(String message) {
        if (!event.isAcknowledged()) {
            event.reply(message).queue();
        } else {
            event.getHook().sendMessage(message).queue();
        }
    }

    /**
     * Reply to the interaction ephemerally (only visible to the user).
     *
     * @param message The message to send.
     */
    public void replyEphemeral(String message) {
        if (!event.isAcknowledged()) {
            event.reply(message).setEphemeral(true).queue();
        } else {
            event.getHook().sendMessage(message).setEphemeral(true).queue();
        }
    }

    /**
     * Defer the reply to the interaction.
     * This is useful for long-running commands.
     */
    public void deferReply() {
        if (!event.isAcknowledged()) {
            event.deferReply().queue();
        }
    }

    /**
     * Defer the reply to the interaction ephemerally.
     */
    public void deferReplyEphemeral() {
        if (!event.isAcknowledged()) {
            event.deferReply(true).queue();
        }
    }
}