package fr.traqueur.commands.adventure;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.logging.MessageDispatcher;
import fr.traqueur.commands.api.logging.MessageKey;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Plugs a {@link ComponentMessageFormatter} into a command manager.
 * <p>
 * The Paper and Velocity managers do this in their {@code setComponentFormatter}; use this
 * class for another Adventure platform.
 * </p>
 *
 * @since 5.5.0
 */
public final class ComponentMessages {

    private ComponentMessages() {
    }

    /**
     * Install a component formatter on a manager whose senders are Adventure audiences.
     *
     * @param manager   The manager.
     * @param formatter The formatter, or null to send every message as text again.
     * @param <S>       The type of the sender.
     */
    public static <S extends Audience> void install(CommandManager<?, S> manager, ComponentMessageFormatter<S> formatter) {
        install(manager, formatter, Audience::sendMessage);
    }

    /**
     * Install a component formatter on a manager, with the way a component reaches a sender.
     *
     * @param manager   The manager.
     * @param formatter The formatter, or null to send every message as text again.
     * @param sender    Sends a component to a sender.
     * @param <S>       The type of the sender.
     */
    public static <S> void install(CommandManager<?, S> manager, ComponentMessageFormatter<S> formatter,
                                   BiConsumer<S, Component> sender) {
        manager.setMessageDispatcher(formatter == null ? null : dispatcher(formatter, sender));
    }

    /**
     * Turn a component formatter into the dispatcher of the core.
     *
     * @param formatter The formatter.
     * @param sender    Sends a component to a sender.
     * @param <S>       The type of the sender.
     * @return The dispatcher.
     */
    public static <S> MessageDispatcher<S> dispatcher(ComponentMessageFormatter<S> formatter, BiConsumer<S, Component> sender) {
        return new Dispatcher<>(formatter, sender);
    }

    /**
     * Get back the component formatter installed on a manager.
     *
     * @param manager The manager.
     * @param <S>     The type of the sender.
     * @return The formatter, or null when none is installed.
     */
    @SuppressWarnings("unchecked")
    public static <S> ComponentMessageFormatter<S> formatterOf(CommandManager<?, S> manager) {
        return manager.getMessageDispatcher() instanceof Dispatcher<?> dispatcher
                ? (ComponentMessageFormatter<S>) dispatcher.formatter
                : null;
    }

    private record Dispatcher<S>(ComponentMessageFormatter<S> formatter,
                                 BiConsumer<S, Component> sender) implements MessageDispatcher<S> {

        @Override
        public boolean dispatch(S receiver, MessageKey key, Map<String, String> placeholders) {
            Component component = formatter.format(receiver, key, placeholders);
            if (component == null) {
                return false;
            }
            sender.accept(receiver, component);
            return true;
        }
    }
}
