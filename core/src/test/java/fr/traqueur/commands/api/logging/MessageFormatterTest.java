package fr.traqueur.commands.api.logging;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandPlatform;
import fr.traqueur.commands.api.requirements.Requirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class MessageFormatterTest {

    private CommandPlatform<String, String> platform;
    private MessageHandler messageHandler;
    private CommandManager<String, String> manager;
    private DummyCommand cmd;

    @BeforeEach
    void setup() {
        platform = mock(CommandPlatform.class);
        messageHandler = mock(MessageHandler.class);

        manager = new CommandManager<>(platform) {
            @Override
            public MessageHandler getMessageHandler() {
                return messageHandler;
            }
        };

        when(platform.isPlayer(anyString())).thenReturn(true);
        when(platform.hasPermission(anyString(), anyString())).thenReturn(true);

        when(messageHandler.getNoPermissionMessage()).thenReturn("NO_PERMISSION");
        when(messageHandler.getArgNotRecognized()).thenReturn("ARG_ERR %arg%");
        when(messageHandler.getRequirementMessage()).thenReturn("REQ_ERR %requirement%");

        cmd = new DummyCommand();
        manager.getCommands().addCommand("base", cmd);
    }

    @Test
    void noFormatter_messageIsSentUnchanged() {
        assertNull(manager.getMessageFormatter());
        denyPermission("user");

        manager.getInvoker().invoke("user", "base", new String[]{});

        verify(platform).sendMessage("user", "NO_PERMISSION");
    }

    @Test
    void noFormatter_formatMessageReturnsTheSameInstance() {
        String message = "NO_PERMISSION";

        assertSame(message, manager.formatMessage("user", message));
    }

    @Test
    void formatter_transformsMessageAndSeesTheTargetedSender() {
        List<String> recipients = new ArrayList<>();
        manager.setMessageFormatter((sender, raw) -> {
            recipients.add(sender);
            return "[prefix] " + raw;
        });
        denyPermission("bob");

        manager.getInvoker().invoke("bob", "base", new String[]{});

        verify(platform).sendMessage("bob", "[prefix] NO_PERMISSION");
        assertEquals(List.of("bob"), recipients);
    }

    @Test
    void formatter_seesTheMessageAfterArgSubstitution() {
        AtomicReference<String> seen = new AtomicReference<>();
        manager.setMessageFormatter((sender, raw) -> {
            seen.set(raw);
            return raw;
        });
        cmd.addArgs("a", Integer.class);

        manager.getInvoker().invoke("user", "base", new String[]{"bad"});

        assertEquals("ARG_ERR bad", seen.get());
        verify(platform).sendMessage("user", "ARG_ERR bad");
    }

    @Test
    void formatter_seesTheMessageAfterRequirementSubstitution() {
        AtomicReference<String> seen = new AtomicReference<>();
        manager.setMessageFormatter((sender, raw) -> {
            seen.set(raw);
            return raw;
        });
        Requirement<String> requirement = mock(Requirement.class);
        when(requirement.check(anyString())).thenReturn(false);
        when(requirement.errorMessage()).thenReturn("");
        cmd.addRequirements(requirement);

        manager.getInvoker().invoke("user", "base", new String[]{});

        assertEquals("REQ_ERR " + requirement.getClass().getSimpleName(), seen.get());
    }

    @Test
    void formatter_appliesToTheHardcodedInternalError() {
        manager.setMessageFormatter((sender, raw) -> "[prefix] " + raw);
        cmd.addArgs("id", UUID.class);

        assertFalse(manager.getInvoker().invoke("user", "base", new String[]{"whatever"}));

        verify(platform).sendMessage("user", "[prefix] &cInternal error: invalid argument type");
    }

    @Test
    void formatter_canBeReplacedThenRemoved() {
        denyPermission("user");

        manager.setMessageFormatter((sender, raw) -> "<first> " + raw);
        manager.getInvoker().invoke("user", "base", new String[]{});
        verify(platform).sendMessage("user", "<first> NO_PERMISSION");

        manager.setMessageFormatter((sender, raw) -> "<second> " + raw);
        manager.getInvoker().invoke("user", "base", new String[]{});
        verify(platform).sendMessage("user", "<second> NO_PERMISSION");

        manager.setMessageFormatter(null);
        manager.getInvoker().invoke("user", "base", new String[]{});
        verify(platform).sendMessage("user", "NO_PERMISSION");
        assertNull(manager.getMessageFormatter());
    }

    private void denyPermission(String sender) {
        cmd.setPermission("perm");
        when(platform.hasPermission(sender, "perm")).thenReturn(false);
    }

    static class DummyCommand extends Command<String, String> {
        DummyCommand() {
            super(null, "base");
        }

        @Override
        public void execute(String sender, Arguments args) {
        }
    }
}
