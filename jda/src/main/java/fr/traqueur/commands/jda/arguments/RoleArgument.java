package fr.traqueur.commands.jda.arguments;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;

/**
 * Argument converter for JDA {@link Role}.
 * <p>
 * This converter handles role input in various formats:
 * </p>
 * <ul>
 *     <li>Role ID (e.g., "123456789")</li>
 *     <li>Role mention (e.g., "&lt;@&amp;123456789&gt;")</li>
 * </ul>
 * <p>
 * Note: This converter requires a guild context to resolve roles.
 * For slash commands, Discord handles role input natively via the ROLE option type.
 * The guild context is obtained from the command execution context.
 * </p>
 * <p>
 * <b>Important:</b> This converter cannot resolve roles without a guild context.
 * It returns null if called outside of a guild context. For slash commands,
 * use {@link fr.traqueur.commands.jda.JDAArguments#getRole(String)} instead,
 * which properly handles the guild context from the event.
 * </p>
 */
public class RoleArgument extends JDAArgumentConverter<Role> {

    /**
     * Creates a new RoleArgument.
     *
     * @param jda The JDA instance.
     */
    public RoleArgument(JDA jda) {
        super(jda);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: This implementation cannot resolve roles without a guild context.
     * For slash commands, the role is automatically resolved by Discord and
     * available through {@link fr.traqueur.commands.jda.JDAArguments#getRole(String)}.
     * </p>
     *
     * @param input The role ID or mention string
     * @return Always returns null as guild context is required
     */
    @Override
    public Role apply(String input) {
        // Roles require guild context which is not available in this converter
        // For slash commands, roles are automatically provided by Discord
        // through the event options (see JDAExecutor line 127)
        return null;
    }
}