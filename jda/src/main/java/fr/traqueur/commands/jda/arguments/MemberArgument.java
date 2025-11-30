package fr.traqueur.commands.jda.arguments;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;

/**
 * Argument converter for JDA {@link Member}.
 * <p>
 * This converter handles member input in various formats:
 * </p>
 * <ul>
 *     <li>User ID (e.g., "123456789")</li>
 *     <li>User mention (e.g., "&lt;@123456789&gt;" or "&lt;@!123456789&gt;")</li>
 * </ul>
 * <p>
 * Note: This converter requires a guild context to resolve members.
 * For slash commands, Discord handles member input natively via the USER option type.
 * The guild context is obtained from the command execution context.
 * </p>
 * <p>
 * <b>Important:</b> This converter cannot resolve members without a guild context.
 * It returns null if called outside of a guild context. For slash commands,
 * use {@link fr.traqueur.commands.jda.JDAArguments#getMember(String)} instead,
 * which properly handles the guild context from the event.
 * </p>
 */
public class MemberArgument extends JDAArgumentConverter<Member> {

    /**
     * Creates a new MemberArgument.
     *
     * @param jda The JDA instance.
     */
    public MemberArgument(JDA jda) {
        super(jda);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: This implementation cannot resolve members without a guild context.
     * For slash commands, the member is automatically resolved by Discord and
     * available through {@link fr.traqueur.commands.jda.JDAArguments#getMember(String)}.
     * </p>
     *
     * @param input The user ID or mention string
     * @return Always returns null as guild context is required
     */
    @Override
    public Member apply(String input) {
        // Members require guild context which is not available in this converter
        // For slash commands, members are automatically provided by Discord
        // through the event options (see JDAExecutor lines 122-124)
        return null;
    }
}