package fr.traqueur.commands.api.models;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.logging.MessageHandler;
import fr.traqueur.commands.api.requirements.Requirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class CommandInvokerTest {

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

        when(messageHandler.getArgNotRecognized())
                .thenReturn("ARG_ERR %arg%");

        cmd = new DummyCommand();
        manager.getCommands().addCommand("base", cmd);
    }


    @Test
    void invoke_unknownCommand_returnsFalse() {
        assertFalse(manager.getInvoker().invoke("user", "unknown", new String[]{"x"}));
    }

    @Test
    void invoke_inGameOnly_nonPlayer_sendsOnlyInGame() {
        cmd.setGameOnly(true);
        when(platform.isPlayer("user")).thenReturn(false);
        when(messageHandler.getOnlyInGameMessage()).thenReturn("ONLY_IN_GAME");

        manager.getInvoker().invoke("user", "base", new String[]{});
        verify(platform).sendMessage("user", "ONLY_IN_GAME");
    }

    @Test
    void invoke_noPermission_sendsNoPermission() {
        cmd.setPermission("perm");
        when(platform.hasPermission("user", "perm")).thenReturn(false);
        when(messageHandler.getNoPermissionMessage()).thenReturn("NO_PERMISSION");

        manager.getInvoker().invoke("user", "base", new String[]{});
        verify(platform).sendMessage("user", "NO_PERMISSION");
    }

    @Test
    void invoke_requirementFails_sendsRequirementError() {
        Requirement<String> req = mock(Requirement.class);
        when(req.check(anyString())).thenReturn(false);
        when(req.errorMessage()).thenReturn("REQ_ERR");
        cmd.addRequirements(req);

        manager.getInvoker().invoke("user", "base", new String[]{});
        verify(platform).sendMessage("user", "REQ_ERR");
    }

    @Test
    void invoke_wrongArgCount_sendsUsage() {
        cmd.addArgs("a", String.class);
        cmd.setUsage("/base <a>");

        manager.getInvoker().invoke("user", "base", new String[]{});
        verify(platform).sendMessage("user", "/base <a>");
    }

    @Test
    void invoke_parseError_sendsArgNotRecognized() {
        cmd.addArgs("a", Integer.class);

        manager.getInvoker().invoke("user", "base", new String[]{"bad"});
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

        manager.getCommands().addCommand("exec", custom);

        boolean result = manager.getInvoker().invoke("user", "exec", new String[]{"hello"});
        assertTrue(result);
        assertTrue(executed.get());
    }

    @Test
    void aliasWithSubCommand_executesSubCommand() {
        cmd.addAlias("base.sub");

        DummyCommand sub = new DummyCommand();
        cmd.addSubCommand(sub);

        manager.getCommands().addCommand("base.sub", cmd);
        manager.getCommands().addCommand("base.sub.base", sub);

        List<String> suggests = manager.getInvoker().suggest("user", "base", new String[]{""});
        assertTrue(suggests.contains("sub"));
    }

    // --- v5.0.0 new tests ---

    @Test
    void invoke_disabledCommand_sendsDisabledMessage() {
        cmd.setEnabled(false);
        when(messageHandler.getCommandDisabledMessage()).thenReturn("COMMAND_DISABLED");

        boolean result = manager.getInvoker().invoke("user", "base", new String[]{});

        assertTrue(result); // Command was found, message sent
        verify(platform).sendMessage("user", "COMMAND_DISABLED");
    }

    @Test
    void invoke_disabledCommand_doesNotExecute() {
        AtomicBoolean executed = new AtomicBoolean(false);

        DummyCommand trackedCmd = new DummyCommand() {
            @Override
            public void execute(String sender, Arguments arguments) {
                executed.set(true);
            }
        };
        trackedCmd.setEnabled(false);
        manager.getCommands().addCommand("tracked", trackedCmd);

        when(messageHandler.getCommandDisabledMessage()).thenReturn("DISABLED");

        manager.getInvoker().invoke("user", "tracked", new String[]{});

        assertFalse(executed.get());
    }

    @Test
    void invoke_reEnabledCommand_executesNormally() {
        AtomicBoolean executed = new AtomicBoolean(false);

        DummyCommand trackedCmd = new DummyCommand() {
            @Override
            public void execute(String sender, Arguments arguments) {
                executed.set(true);
            }
        };

        // Disable then re-enable
        trackedCmd.setEnabled(false);
        trackedCmd.setEnabled(true);

        manager.getCommands().addCommand("tracked", trackedCmd);

        boolean result = manager.getInvoker().invoke("user", "tracked", new String[]{});

        assertTrue(result);
        assertTrue(executed.get());
    }

    @Test
    void invoke_enabledByDefault_executesNormally() {
        AtomicBoolean executed = new AtomicBoolean(false);

        DummyCommand trackedCmd = new DummyCommand() {
            @Override
            public void execute(String sender, Arguments arguments) {
                executed.set(true);
            }
        };

        // Don't call setEnabled - should be enabled by default
        manager.getCommands().addCommand("tracked", trackedCmd);

        boolean result = manager.getInvoker().invoke("user", "tracked", new String[]{});

        assertTrue(result);
        assertTrue(executed.get());
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