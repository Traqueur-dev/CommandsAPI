package fr.traqueur.commands.jda.arguments;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;

/**
 * Argument converter for JDA {@link GuildChannelUnion}.
 * <p>
 * This converter handles channel input in various formats:
 * </p>
 * <ul>
 *     <li>Channel ID (e.g., "123456789")</li>
 *     <li>Channel mention (e.g., "&lt;#123456789&gt;")</li>
 * </ul>
 * <p>
 * Note: This converter requires a guild context to resolve channels.
 * For slash commands, Discord handles channel input natively via the CHANNEL option type.
 * The guild context is obtained from the command execution context.
 * </p>
 * <p>
 * <b>Important:</b> This converter cannot resolve channels without a guild context.
 * It returns null if called outside of a guild context. For slash commands,
 * use {@link fr.traqueur.commands.jda.JDAArguments#getChannel(String)} instead,
 * which properly handles the guild context from the event.
 * </p>
 */
public class ChannelArgument extends JDAArgumentConverter<GuildChannelUnion> {

    /**
     * Creates a new ChannelArgument.
     *
     * @param jda The JDA instance.
     */
    public ChannelArgument(JDA jda) {
        super(jda);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: This implementation cannot resolve channels without a guild context.
     * For slash commands, the channel is automatically resolved by Discord and
     * available through {@link fr.traqueur.commands.jda.JDAArguments#getChannel(String)}.
     * </p>
     *
     * @param input The channel ID or mention string
     * @return Always returns null as guild context is required
     */
    @Override
    public GuildChannelUnion apply(String input) {
        // Channels require guild context which is not available in this converter
        // For slash commands, channels are automatically provided by Discord
        // through the event options (see JDAExecutor lines 129-131)
        return null;
    }
}