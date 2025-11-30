package fr.traqueur.commands.jda.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for JDA argument converters.
 * <p>
 * This class provides access to the JDA instance for converters that need it.
 * All JDA-specific argument converters should extend this class.
 * </p>
 *
 * @param <T> The type this converter produces.
 */
public abstract class JDAArgumentConverter<T> implements ArgumentConverter<T>, TabCompleter<SlashCommandInteractionEvent> {

    /**
     * The JDA instance used for resolving Discord entities.
     */
    protected final JDA jda;

    /**
     * Creates a new JDA argument converter.
     *
     * @param jda The JDA instance.
     */
    public JDAArgumentConverter(JDA jda) {
        this.jda = jda;
    }

    /**
     * {@inheritDoc}
     * <p>
     * For slash commands, autocomplete is handled by Discord natively.
     * This method returns an empty list by default.
     * Subclasses can override this if needed for non-slash command contexts.
     * </p>
     */
    @Override
    public List<String> onCompletion(SlashCommandInteractionEvent sender, List<String> args) {
        // Slash commands handle autocomplete natively through Discord
        return Collections.emptyList();
    }
}