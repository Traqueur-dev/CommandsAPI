package fr.traqueur.commands.jda.arguments;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;

/**
 * Argument converter for JDA {@link Message.Attachment}.
 * <p>
 * This converter handles attachment input for Discord messages.
 * </p>
 * <p>
 * Note: Attachments are only available in the context of slash commands or message events.
 * For slash commands, Discord handles attachment input natively via the ATTACHMENT option type.
 * </p>
 * <p>
 * <b>Important:</b> This converter cannot resolve attachments from a string input.
 * It returns null when called. For slash commands, attachments are automatically provided
 * by Discord through the event options (see JDAExecutor lines 136-138).
 * </p>
 */
public class AttachmentArgument extends JDAArgumentConverter<Message.Attachment> {

    /**
     * Creates a new AttachmentArgument.
     *
     * @param jda The JDA instance.
     */
    public AttachmentArgument(JDA jda) {
        super(jda);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: This implementation cannot resolve attachments from string input.
     * For slash commands, attachments are automatically provided by Discord
     * through the event options.
     * </p>
     *
     * @param input The input string (unused for attachments)
     * @return Always returns null as attachments cannot be resolved from strings
     */
    @Override
    public Message.Attachment apply(String input) {
        // Attachments cannot be resolved from string input
        // They are automatically provided by Discord in slash commands
        // through the event options (see JDAExecutor lines 136-138)
        return null;
    }
}