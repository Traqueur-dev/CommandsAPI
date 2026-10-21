package fr.traqueur.commands.adventure;

import fr.traqueur.commands.api.logging.MessageHandler;
import fr.traqueur.commands.api.logging.MessageKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;
import java.util.function.Function;

/**
 * Builds the {@link Component} of a library message, on the platforms that speak Adventure.
 * <p>
 * Unlike the text {@link fr.traqueur.commands.api.logging.MessageFormatter}, it receives the
 * message by its {@link MessageKey} and its values apart ({@code arg}, {@code requirement},
 * {@code usage}, see {@link MessageKey}), never substituted in a text: a value typed by a
 * player cannot inject formatting, and click and hover events reach the sender intact, because
 * the component is sent as a component.
 * </p>
 * <pre>{@code
 * manager.setComponentFormatter(ComponentMessageFormatter.miniMessage(key -> config.getString("messages." + key)));
 * }</pre>
 *
 * @param <S> The type of the sender the message is sent to.
 * @since 5.5.0
 */
@FunctionalInterface
public interface ComponentMessageFormatter<S> {

    /**
     * Build a message.
     *
     * @param receiver     The sender the message is sent to.
     * @param key          The message.
     * @param placeholders Its named values, see {@link MessageKey}.
     * @return The component to send, or null to send the message as text, as without a
     * component formatter.
     */
    Component format(S receiver, MessageKey key, Map<String, String> placeholders);

    /**
     * A formatter reading MiniMessage templates, with the values as {@code <arg>},
     * {@code <requirement>} and {@code <usage>} tags.
     * <p>
     * The values are inserted as plain text: a value holding MiniMessage tags is shown as typed,
     * it is not parsed. The three tags are known for every key, empty when the key has no such
     * value, so one template can serve several keys. A key whose template is null falls back to
     * the text path.
     * </p>
     *
     * @param templates The MiniMessage template of each key.
     * @param <S>       The type of the sender.
     * @return The formatter.
     */
    static <S> ComponentMessageFormatter<S> miniMessage(Function<MessageKey, String> templates) {
        return miniMessage(MiniMessage.miniMessage(), templates);
    }

    /**
     * A formatter reading MiniMessage templates with a given MiniMessage instance.
     *
     * @param miniMessage The MiniMessage instance, with its own tags.
     * @param templates   The MiniMessage template of each key.
     * @param <S>         The type of the sender.
     * @return The formatter.
     * @see #miniMessage(Function)
     */
    static <S> ComponentMessageFormatter<S> miniMessage(MiniMessage miniMessage, Function<MessageKey, String> templates) {
        return (receiver, key, placeholders) -> {
            String template = templates.apply(key);
            if (template == null) {
                return null;
            }
            Map<String, String> values = new java.util.HashMap<>();
            values.put(MessageKey.ARG, "");
            values.put(MessageKey.REQUIREMENT_VALUE, "");
            values.put(MessageKey.USAGE_VALUE, "");
            values.putAll(placeholders);
            TagResolver.Builder resolver = TagResolver.builder();
            values.forEach((name, value) -> resolver.resolver(Placeholder.unparsed(name, value)));
            return miniMessage.deserialize(template, resolver.build());
        };
    }

    /**
     * A formatter reading MiniMessage templates from a {@link MessageHandler}.
     * <p>
     * The legacy {@code %arg%}, {@code %requirement%} and {@code %usage%} placeholders of the
     * handler's texts are read as the {@code <arg>}, {@code <requirement>} and {@code <usage>} tags.
     * The usage, which the handler does not hold, is sent as plain text.
     * </p>
     *
     * @param handler The handler holding MiniMessage texts.
     * @param <S>     The type of the sender.
     * @return The formatter.
     */
    static <S> ComponentMessageFormatter<S> miniMessage(MessageHandler handler) {
        return miniMessage(key -> {
            if (key == MessageKey.USAGE) {
                return "<" + MessageKey.USAGE_VALUE + ">";
            }
            String text = handler.getMessage(key);
            if (text == null) {
                return null;
            }
            return text.replace("%" + MessageKey.ARG + "%", "<" + MessageKey.ARG + ">")
                    .replace("%" + MessageKey.REQUIREMENT_VALUE + "%", "<" + MessageKey.REQUIREMENT_VALUE + ">")
                    .replace("%" + MessageKey.USAGE_VALUE + "%", "<" + MessageKey.USAGE_VALUE + ">");
        });
    }
}
