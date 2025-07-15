package fr.traqueur.commands.api.models;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.logging.MessageHandler;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.api.requirements.Requirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class CommandInvokerTest {

    private CommandManager<String, String> manager;
    private CommandTree<String, String> tree;
    private CommandPlatform<String, String> platform;
    private MessageHandler messageHandler;
    private CommandInvoker<String, String> invoker;
    private DummyCommand cmd;

    @BeforeEach
    void setup() {
        platform = mock(CommandPlatform.class);
        messageHandler = mock(MessageHandler.class);
        manager = mock(CommandManager.class);
        when(manager.getPlatform()).thenReturn(platform);
        when(manager.getMessageHandler()).thenReturn(messageHandler);
        when(platform.isPlayer(anyString())).thenReturn(true);
        when(platform.hasPermission(anyString(), anyString())).thenReturn(true);

        cmd = new DummyCommand();
        tree = new CommandTree<>();
        tree.addCommand("base",cmd);
        when(manager.getCommands()).thenReturn(tree);

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
        when(manager.parse(eq(cmd), any(String[].class)))
                .thenThrow(new ArgumentIncorrectException("bad"));
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

        tree = new CommandTree<>();
        tree.addCommand("base",custom);
        when(manager.getCommands()).thenReturn(tree);
        invoker = new CommandInvoker<>(manager);

        boolean result = invoker.invoke("user", "base", new String[]{"hello"});
        assertTrue(result);
        assertTrue(executed.get());
    }

    @Test
    void valid_AliasWithSubCommand_executesSubCommand() {
        cmd.addAlias("base.sub");

        DummyCommand sub = new DummyCommand();
        cmd.addSubCommand(sub);

        tree.addCommand("base.sub", cmd);
        tree.addCommand("base.sub.base", sub);
        tree.addCommand("base.base", sub);

        List<String> suggests = invoker.suggest("user", "base", new String[]{""});
        assertTrue(suggests.contains("sub"));
        assertTrue(suggests.contains("base"));

        List<String> suggests3 = invoker.suggest("user", "base", new String[]{"sub"});
        assertTrue(suggests3.contains("sub"));

        List<String> suggests4 = invoker.suggest("user", "base", new String[]{"sub", ""});
        assertTrue(suggests4.contains("base"));
    }

    static class DummyCommand extends Command<String, String> {
        DummyCommand() { super(null, "base"); }
        @Override public void execute(String sender, Arguments args) {}
    }
}
