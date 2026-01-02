package fr.traqueur.commands.jda.requirements;

import fr.traqueur.commands.api.requirements.Requirement;
import fr.traqueur.commands.jda.JDAInteractionContext;

import java.util.Arrays;
import java.util.Collection;

/**
 * Requirement that checks if the command is executed in a specific guild.
 */
public class GuildRequirement implements Requirement<JDAInteractionContext> {

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
    public boolean check(JDAInteractionContext context) {
        if (context.getGuild() == null) {
            return false;
        }
        return guildIds.contains(context.getGuild().getIdLong());
    }

    @Override
    public String errorMessage() {
        return errorMessage;
    }
}
