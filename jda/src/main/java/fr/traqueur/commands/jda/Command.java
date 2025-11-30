package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.arguments.Arguments;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Abstract base class for JDA slash commands.
 * This class extends the base Command class and provides JDA-specific functionality.
 *
 * @param <T> The type of the bot instance.
 */
public abstract class Command<T> extends fr.traqueur.commands.api.models.Command<T, SlashCommandInteractionEvent> {

    /**
     * Constructor for a JDA command.
     *
     * @param bot  The bot instance.
     * @param name The name of the command.
     */
    public Command(T bot, String name) {
        super(bot, name);
    }

    /**
     * Abstract method to execute the command.
     * Must be implemented by subclasses to define command behavior.
     *
     * @param event     The slash command interaction event.
     * @param arguments The JDA-specific arguments for the command.
     */
    public abstract void execute(SlashCommandInteractionEvent event, JDAArguments arguments);

    public void execute(SlashCommandInteractionEvent event, Arguments arguments) {
        this.execute(event, jda(arguments));
    }

    /**
     * Helper method to cast Arguments to JDAArguments.
     * This provides easy access to JDA-specific argument methods.
     *
     * @param arguments The arguments to cast.
     * @return The arguments as JDAArguments.
     */
    protected JDAArguments jda(Arguments arguments) {
        return (JDAArguments) arguments;
    }

    /**
     * Helper method to get the event's user.
     *
     * @param event The slash command event.
     * @return The user who triggered the command.
     */
    protected User getUser(SlashCommandInteractionEvent event) {
        return event.getUser();
    }

    /**
     * Helper method to get the event's member (null if not in a guild).
     *
     * @param event The slash command event.
     * @return The member who triggered the command, or null if not in a guild.
     */
    protected Member getMember(SlashCommandInteractionEvent event) {
        return event.getMember();
    }

    /**
     * Helper method to get the event's guild (null if not in a guild).
     *
     * @param event The slash command event.
     * @return The guild where the command was triggered, or null if not in a guild.
     */
    protected Guild getGuild(SlashCommandInteractionEvent event) {
        return event.getGuild();
    }

    /**
     * Helper method to get the event's channel.
     *
     * @param event The slash command event.
     * @return The channel where the command was triggered.
     */
    protected MessageChannelUnion getChannel(SlashCommandInteractionEvent event) {
        return event.getChannel();
    }
}