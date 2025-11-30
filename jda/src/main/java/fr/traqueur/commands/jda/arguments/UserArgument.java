package fr.traqueur.commands.jda.arguments;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

/**
 * Argument converter for JDA {@link User}.
 * <p>
 * This converter handles user input in various formats:
 * </p>
 * <ul>
 *     <li>User ID (e.g., "123456789")</li>
 *     <li>User mention (e.g., "&lt;@123456789&gt;" or "&lt;@!123456789&gt;")</li>
 * </ul>
 * <p>
 * For slash commands, Discord handles user input natively via the USER option type.
 * This converter is primarily useful for text-based command contexts.
 * </p>
 */
public class UserArgument extends JDAArgumentConverter<User> {

    /**
     * Creates a new UserArgument.
     *
     * @param jda The JDA instance.
     */
    public UserArgument(JDA jda) {
        super(jda);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Converts a user ID or mention to a User object.
     * Accepts both raw IDs (e.g., "123456789") and mentions (e.g., "&lt;@123456789&gt;").
     * </p>
     *
     * @param input The user ID or mention string
     * @return The User object, or null if not found or invalid
     */
    @Override
    public User apply(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        // Remove mention formatting if present (<@123456789> or <@!123456789>)
        String id = input.replaceAll("[<@!>]", "");

        try {
            long userId = Long.parseLong(id);
            return jda.retrieveUserById(userId).complete();
        } catch (NumberFormatException e) {
            return null;
        } catch (Exception e) {
            // User not found or other JDA exception
            return null;
        }
    }
}