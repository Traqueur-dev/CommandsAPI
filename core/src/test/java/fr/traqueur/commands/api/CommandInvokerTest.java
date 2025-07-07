package fr.traqueur.commands.api;

import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.logging.MessageHandler;
import fr.traqueur.commands.api.requirements.Requirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings(value = "unchecked")
class CommandInvokerTest {

    private CommandManager<String, String> manager;
    private CommandPlatform<String, String> platform;
    private MessageHandler messageHandler;
    private CommandInvoker<String, String> invoker;
    private DummyCommand cmd;

    @BeforeEach
    void setup() {
        // Mock platform and manager
        platform = mock(CommandPlatform.class);
        messageHandler = mock(MessageHandler.class);
        manager = mock(CommandManager.class);
        when(manager.getPlatform()).thenReturn(platform);
        when(manager.getMessageHandler()).thenReturn(messageHandler);
        // Default platform behaviors
        when(platform.isPlayer(anyString())).thenReturn(true);
        when(platform.hasPermission(anyString(), anyString())).thenReturn(true);
        // Register single command under key "base"
        cmd = new DummyCommand();
        Map<String, Command<String, String>> map = new HashMap<>();
        map.put("base", cmd);
        when(manager.getCommands()).thenReturn(map);
        // Create invoker
        invoker = new CommandInvoker<>(manager);
    }

    @Test
    void invoke_unknownCommand_returnsFalse() {
        boolean res = invoker.invoke("user", "other", new String[]{"x"});
        assertFalse(res);
        verifyNoInteractions(platform);
    }

    @Test
    void invoke_inGameOnly_nonPlayer_sendsOnlyInGame() {
        cmd.setGameOnly(true);
        when(platform.isPlayer("user")).thenReturn(false);
        when(messageHandler.getOnlyInGameMessage()).thenReturn("ONLY_IN_GAME");

        invoker.invoke("user", "base", new String[]{});

        verify(platform).sendMessage("user", "ONLY_IN_GAME");
    }

    @Test
    void invoke_noPermission_sendsNoPermission() {
        cmd.setPermission("perm");
        when(platform.hasPermission("user", "perm")).thenReturn(false);
        when(messageHandler.getNoPermissionMessage()).thenReturn("NO_PERMISSION");

        invoker.invoke("user", "base", new String[]{});

        verify(platform).sendMessage("user", "NO_PERMISSION");
    }

    @Test
    void invoke_requirementFails_sendsRequirementError() {
        Requirement<String> req = mock(Requirement.class);
        when(req.check(anyString())).thenReturn(false);
        when(req.errorMessage()).thenReturn("REQ_ERR");
        cmd.addRequirements(req);

        invoker.invoke("user", "base", new String[]{});

        verify(platform).sendMessage("user", "REQ_ERR");
    }

    @Test
    void invoke_wrongArgCount_sendsUsage() {
        cmd.addArgs("a", String.class);
        cmd.setUsage("/base <a>");

        invoker.invoke("user", "base", new String[]{});

        verify(platform).sendMessage("user", "/base <a>");
    }

    @Test
    void invoke_parseThrowsArgumentIncorrect_sendsArgNotRecognized() throws Exception {
        cmd.addArgs("a", String.class);
        when(manager.parse(eq(cmd), any(String[].class))).thenThrow(new ArgumentIncorrectException("bad"));
        when(messageHandler.getArgNotRecognized()).thenReturn("ARG_ERR %arg%");

        invoker.invoke("user", "base", new String[]{"bad"});

        verify(platform).sendMessage("user", "ARG_ERR bad");
    }

    @Test
    void invoke_valid_executesCommand_andReturnsTrue() {
        AtomicBoolean executed = new AtomicBoolean(false);
        DummyCommand custom = new DummyCommand() {
            @Override
            public void execute(String sender, Arguments arguments) {
                executed.set(true);
            }
        };
        custom.addArgs("x", String.class);
        when(manager.getCommands()).thenReturn(Collections.singletonMap("base", custom));

        boolean result = invoker.invoke("user", "base", new String[]{"hello"});

        assertTrue(result);
        assertTrue(executed.get());
    }

    static class DummyCommand extends Command<String, String> {
        DummyCommand() { super(null, "base"); }
        @Override public void execute(String sender, Arguments args) {}
    }
}
