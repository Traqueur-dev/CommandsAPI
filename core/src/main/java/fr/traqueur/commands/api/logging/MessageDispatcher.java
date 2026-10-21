package fr.traqueur.commands.api.logging;

import java.util.Map;

/**
 * Sends a library message by its key, instead of as text.
 * <p>
 * This is the hook a platform uses to send rich messages, such as Adventure components: it
 * receives the {@link MessageKey} and the named values of the message, not a string where
 * they are already substituted. Most code does not implement it directly, but goes through
 * the platform's own API, such as {@code setComponentFormatter} on Paper and Velocity.
 * </p>
 * <p>
 * Returning false hands the message back to the text path: the {@link MessageHandler} text,
 * with its placeholders substituted, through the {@link MessageFormatter}, sent as a string.
 * </p>
 *
 * @param <S> The type of the sender the message is sent to.
 * @since 5.5.0
 */
@FunctionalInterface
public interface MessageDispatcher<S> {

    /**
     * Send a message to its recipient.
     *
     * @param receiver     The sender the message is sent to.
     * @param key          The message.
     * @param placeholders The named values of the message, see {@link MessageKey}.
     * @return true when the message was sent, false to send it as text instead.
     */
    boolean dispatch(S receiver, MessageKey key, Map<String, String> placeholders);
}
