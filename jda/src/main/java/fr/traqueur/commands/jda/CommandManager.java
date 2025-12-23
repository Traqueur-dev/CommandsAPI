package fr.traqueur.commands.jda;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.logging.Logger;

/**
 * JDA-specific CommandManager that manages slash commands for Discord bots.
 * This class extends the base CommandManager and provides Discord-specific functionality.
 *
 * @param <T> The type of the bot instance.
 */
public class CommandManager<T> extends fr.traqueur.commands.api.CommandManager<T, SlashCommandInteractionEvent> {

    /**
     * The JDA platform instance.
     */
    private final JDAPlatform<T> jdaPlatform;

    /**
     * Constructor for the JDA CommandManager.
     *
     * @param bot    The bot instance.
     * @param jda    The JDA instance.
     * @param logger The logger instance.
     */
    public CommandManager(T bot, JDA jda, Logger logger) {
        super(new JDAPlatform<>(bot, jda, logger));
        this.jdaPlatform = (JDAPlatform<T>) getPlatform();
    }

    /**
     * Synchronize all registered commands with Discord globally.
     * Note: Global commands may take up to 1 hour to update.
     */
    public void syncCommands() {
        jdaPlatform.syncCommands();
    }

    /**
     * Synchronize all registered commands with a specific guild.
     * This updates instantly and is useful for testing.
     *
     * @param guildId The guild ID.
     */
    public void syncCommandsToGuild(long guildId) {
        jdaPlatform.syncCommandsToGuild(guildId);
    }

    /**
     * Synchronize all registered commands with a specific guild.
     *
     * @param guildId The guild ID as a string.
     */
    public void syncCommandsToGuild(String guildId) {
        jdaPlatform.syncCommandsToGuild(guildId);
    }

    /**
     * Get the JDA instance.
     *
     * @return The JDA instance.
     */
    public JDA getJDA() {
        return jdaPlatform.getJDA();
    }

    /**
     * Get the JDA platform.
     *
     * @return The JDA platform.
     */
    public JDAPlatform<T> getJDAPlatform() {
        return jdaPlatform;
    }
}