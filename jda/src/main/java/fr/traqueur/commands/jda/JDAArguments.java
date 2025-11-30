package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.logging.Logger;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Optional;

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
     * Get a User from the command options.
     *
     * @param name The name of the option.
     * @return An Optional containing the User if present.
     */
    public Optional<User> getUser(String name) {
        OptionMapping option = event.getOption(name);
        if (option != null) {
            return Optional.of(option.getAsUser());
        }
        // Fallback to arguments map
        return this.getAs(name, User.class);
    }

    /**
     * Get a User from the command options with a default value.
     *
     * @param name         The name of the option.
     * @param defaultValue The default value if not present.
     * @return The User or the default value.
     */
    public User getUser(String name, User defaultValue) {
        return getUser(name).orElse(defaultValue);
    }

    /**
     * Get a Member from the command options.
     *
     * @param name The name of the option.
     * @return An Optional containing the Member if present.
     */
    public Optional<Member> getMember(String name) {
        OptionMapping option = event.getOption(name);
        if (option != null) {
            return Optional.ofNullable(option.getAsMember());
        }
        // Fallback to arguments map
        return this.getAs(name, Member.class);
    }

    /**
     * Get a Member from the command options with a default value.
     *
     * @param name         The name of the option.
     * @param defaultValue The default value if not present.
     * @return The Member or the default value.
     */
    public Member getMember(String name, Member defaultValue) {
        return getMember(name).orElse(defaultValue);
    }

    /**
     * Get a Role from the command options.
     *
     * @param name The name of the option.
     * @return An Optional containing the Role if present.
     */
    public Optional<Role> getRole(String name) {
        OptionMapping option = event.getOption(name);
        if (option != null) {
            return Optional.of(option.getAsRole());
        }
        // Fallback to arguments map
        return this.getAs(name, Role.class);
    }

    /**
     * Get a Role from the command options with a default value.
     *
     * @param name         The name of the option.
     * @param defaultValue The default value if not present.
     * @return The Role or the default value.
     */
    public Role getRole(String name, Role defaultValue) {
        return getRole(name).orElse(defaultValue);
    }

    /**
     * Get a GuildChannel from the command options.
     *
     * @param name The name of the option.
     * @return An Optional containing the GuildChannel if present.
     */
    public Optional<GuildChannelUnion> getChannel(String name) {
        OptionMapping option = event.getOption(name);
        if (option != null) {
            return Optional.of(option.getAsChannel());
        }
        // Fallback to arguments map
        return this.getAs(name, GuildChannelUnion.class);
    }

    /**
     * Get a GuildChannel from the command options with a default value.
     *
     * @param name         The name of the option.
     * @param defaultValue The default value if not present.
     * @return The GuildChannel or the default value.
     */
    public GuildChannelUnion getChannel(String name, GuildChannelUnion defaultValue) {
        return getChannel(name).orElse(defaultValue);
    }

    /**
     * Override getAsString to first check JDA options.
     *
     * @param argument The key of the argument.
     * @return The string or empty if not present.
     */
    @Override
    public Optional<String> getAsString(String argument) {
        OptionMapping option = event.getOption(argument);
        if (option != null) {
            return Optional.of(option.getAsString());
        }
        return super.getAsString(argument);
    }

    /**
     * Override getAsInt to first check JDA options.
     *
     * @param argument The key of the argument.
     * @return The integer or empty if not present.
     */
    @Override
    public Optional<Integer> getAsInt(String argument) {
        OptionMapping option = event.getOption(argument);
        if (option != null) {
            return Optional.of((int) option.getAsLong());
        }
        return super.getAsInt(argument);
    }

    /**
     * Override getAsLong to first check JDA options.
     *
     * @param argument The key of the argument.
     * @return The long or empty if not present.
     */
    @Override
    public Optional<Long> getAsLong(String argument) {
        OptionMapping option = event.getOption(argument);
        if (option != null) {
            return Optional.of(option.getAsLong());
        }
        return super.getAsLong(argument);
    }

    /**
     * Override getAsDouble to first check JDA options.
     *
     * @param argument The key of the argument.
     * @return The double or empty if not present.
     */
    @Override
    public Optional<Double> getAsDouble(String argument) {
        OptionMapping option = event.getOption(argument);
        if (option != null) {
            return Optional.of(option.getAsDouble());
        }
        return super.getAsDouble(argument);
    }

    /**
     * Override getAsBoolean to first check JDA options.
     *
     * @param argument The key of the argument.
     * @return The boolean or empty if not present.
     */
    @Override
    public Optional<Boolean> getAsBoolean(String argument) {
        OptionMapping option = event.getOption(argument);
        if (option != null) {
            return Optional.of(option.getAsBoolean());
        }
        return super.getAsBoolean(argument);
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