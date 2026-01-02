package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.arguments.Arguments;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

/**
 * Abstract base class for JDA slash commands.
 * This class extends the base Command class and provides JDA-specific functionality.
 *
 * @param <T> The type of the bot instance.
 */
public abstract class Command<T> extends fr.traqueur.commands.api.models.Command<T, JDAInteractionContext> {

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
     * @param context   The JDA interaction context.
     * @param arguments The JDA-specific arguments for the command.
     */
    public abstract void execute(JDAInteractionContext context, JDAArguments arguments);

    public void execute(JDAInteractionContext context, Arguments arguments) {
        this.execute(context, jda(arguments));
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
     * Helper method to get the context's user.
     *
     * @param context The JDA interaction context.
     * @return The user who triggered the command.
     */
    protected User getUser(JDAInteractionContext context) {
        return context.getUser();
    }

    /**
     * Helper method to get the context's member (null if not in a guild).
     *
     * @param context The JDA interaction context.
     * @return The member who triggered the command, or null if not in a guild.
     */
    protected Member getMember(JDAInteractionContext context) {
        return context.getMember();
    }

    /**
     * Helper method to get the context's guild (null if not in a guild).
     *
     * @param context The JDA interaction context.
     * @return The guild where the command was triggered, or null if not in a guild.
     */
    protected Guild getGuild(JDAInteractionContext context) {
        return context.getGuild();
    }

    /**
     * Helper method to get the context's channel.
     *
     * @param context The JDA interaction context.
     * @return The channel where the command was triggered.
     */
    protected MessageChannelUnion getChannel(JDAInteractionContext context) {
        return context.getChannel();
    }
}
