package fr.traqueur.commands.jda.requirements;

import fr.traqueur.commands.api.requirements.Requirement;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Arrays;
import java.util.Collection;

/**
 * Requirement that checks if the command is executed in a specific guild.
 */
public class GuildRequirement implements Requirement<SlashCommandInteractionEvent> {

    private final Collection<Long> guildIds;
    private final String errorMessage;

    /**
     * Constructor for GuildRequirement.
     *
     * @param guildIds The guild IDs where the command is allowed.
     */
    public GuildRequirement(Long... guildIds) {
        this(null, guildIds);
    }

    /**
     * Constructor for GuildRequirement with custom error message.
     *
     * @param errorMessage Custom error message.
     * @param guildIds     The guild IDs where the command is allowed.
     */
    public GuildRequirement(String errorMessage, Long... guildIds) {
        this.guildIds = Arrays.asList(guildIds);
        this.errorMessage = errorMessage != null ? errorMessage :
                "This command can only be used in specific servers.";
    }

    @Override
    public boolean check(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) {
            return false;
        }
        return guildIds.contains(event.getGuild().getIdLong());
    }

    @Override
    public String errorMessage() {
        return errorMessage;
    }
}