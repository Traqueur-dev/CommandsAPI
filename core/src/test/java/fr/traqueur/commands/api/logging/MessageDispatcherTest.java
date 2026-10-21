package fr.traqueur.commands.api.logging;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.impl.logging.InternalMessageHandler;
import fr.traqueur.commands.test.mocks.MockCommandManager;
import fr.traqueur.commands.test.mocks.MockSender;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessageDispatcherTest {

    @Test
    void dispatcher_receivesTheKeyAndTheValues_andTheTextPathIsSkipped() {
        MockCommandManager manager = new MockCommandManager();
        List<String> texts = new ArrayList<>();
        List<Object> dispatched = new ArrayList<>();
        manager.setMessageDispatcher((receiver, key, values) -> dispatched.add(List.of(key, values)));
        Command<Object, MockSender> command = new Command<>(null, "pay") {
            @Override
            public void execute(MockSender sender, Arguments arguments) {
            }
        };
        command.addArgs("amount", Integer.class);
        manager.registerCommand(command);

        manager.getInvoker().invoke(sender(texts), "pay", new String[0]);
        manager.getInvoker().invoke(sender(texts), "pay", new String[]{"abc"});

        assertTrue(texts.isEmpty());
        assertEquals(MessageKey.USAGE, ((List<?>) dispatched.get(0)).get(0));
        assertEquals(List.of(MessageKey.ARG_NOT_RECOGNIZED, Map.of(MessageKey.ARG, "abc")), dispatched.get(1));
    }

    @Test
    void decliningDispatcher_leavesTheTextPathUnchanged() {
        MockCommandManager manager = new MockCommandManager();
        List<String> texts = new ArrayList<>();
        manager.setMessageDispatcher((receiver, key, values) -> false);
        Command<Object, MockSender> command = new Command<>(null, "pay") {
            @Override
            public void execute(MockSender sender, Arguments arguments) {
            }
        };
        command.addArgs("amount", Integer.class);
        manager.registerCommand(command);

        manager.getInvoker().invoke(sender(texts), "pay", new String[]{"abc"});

        assertEquals(List.of("&cArgument &eabc &cnot recognized."), texts);
    }

    @Test
    void handler_givesTheTextOfEveryKey() {
        MessageHandler handler = new InternalMessageHandler();
        assertEquals(handler.getNoPermissionMessage(), handler.getMessage(MessageKey.NO_PERMISSION));
        assertEquals(handler.getRequirementMessage(), handler.getMessage(MessageKey.REQUIREMENT));
        assertEquals(handler.getArgumentTooLongMessage(), handler.getMessage(MessageKey.TOO_LONG));
        assertNull(handler.getMessage(MessageKey.USAGE));
        for (MessageKey key : MessageKey.values()) {
            assertDoesNotThrow(() -> handler.getMessage(key));
        }
    }

    private static MockSender sender(List<String> texts) {
        return new MockSender() {
            @Override
            public void sendMessage(String message) {
                texts.add(message);
            }

            @Override
            public boolean hasPermission(String permission) {
                return true;
            }
        };
    }
}
