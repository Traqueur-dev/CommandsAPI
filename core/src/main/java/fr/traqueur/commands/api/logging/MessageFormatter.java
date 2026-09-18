package fr.traqueur.commands.api.logging;

/**
 * The interface MessageFormatter.
 * <p>
 * A formatter is the last step a message goes through before the library sends it to a
 * sender. It receives the message once every internal placeholder has been substituted,
 * together with the sender it is about to be sent to, and returns the text that is
 * actually emitted.
 * </p>
 * <p>
 * This is the extension point for anything that needs the recipient to be known: a
 * MiniMessage or Adventure pass, a plugin prefix, external placeholders, or a
 * translation lookup based on the sender's locale.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>{@code
 * // Prefix every command message and let MiniMessage render it.
 * manager.setMessageFormatter((sender, raw) -> {
 *     Component component = MiniMessage.miniMessage().deserialize("<gray>[MyPlugin]</gray> " + raw);
 *     return LegacyComponentSerializer.legacySection().serialize(component);
 * });
 * }</pre>
 * <p>
 * Messages are plain {@link String} in and out, because the core module also serves
 * platforms without Adventure, such as JDA. A consumer working with components has to
 * serialize them, usually to the legacy {@code §} format, which drops hover and click
 * events. This is a deliberate limitation: command messages such as "you do not have
 * permission" do not need them, and honouring them would mean pulling Adventure into
 * the core module.
 * </p>
 *
 * @param <S> The type of the sender the message is sent to.
 * @since 5.3.0
 */
@FunctionalInterface
public interface MessageFormatter<S> {

    /**
     * Format a message before it is sent to its recipient.
     *
     * @param sender The sender the message is about to be sent to.
     * @param raw    The message, with the internal placeholders already substituted.
     * @return The message to send.
     */
    String format(S sender, String raw);
}
