package fr.traqueur.commands.api.models;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Arguments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    private static class DummyCommand extends Command<String, Object> {
        DummyCommand(String name) {
            super("plugin", name);
        }
        DummyCommand() {
            super("plugin", "dummy");
        }

        @Override
        public void execute(Object sender, Arguments arguments) {
            // no-op
        }
    }

    private DummyCommand cmd;

    @BeforeEach
    void setUp() {
        CommandPlatform<String, Object> platform = new CommandPlatform<>() {
            @Override
            public String getPlugin() {
                return null;
            }

            @Override
            public void injectManager(CommandManager<String, Object> commandManager) {
            }

            @Override
            public java.util.logging.Logger getLogger() {
                return java.util.logging.Logger.getAnonymousLogger();
            }

            @Override
            public boolean hasPermission(Object sender, String permission) {
                return true;
            }

            @Override
            public boolean isPlayer(Object sender) {
                return false;
            }

            @Override
            public void sendMessage(Object sender, String message) {
            }

            @Override
            public void addCommand(Command<String, Object> command, String label) {
            }

            @Override
            public void removeCommand(String label, boolean subcommand) {
            }
        };
        CommandManager<String, Object> manager = new CommandManager<>(platform) {
        };
        cmd = new DummyCommand();
        cmd.setManager(manager);
    }

    @Test
    void testAliasesAndName() {
        assertEquals("dummy", cmd.getName());
        assertEquals(0, cmd.getAliases().size());
        cmd.addAlias("d1", "d2");
        List<String> aliases = cmd.getAliases();
        assertEquals(2, cmd.getAliases().size());
        assertTrue(aliases.contains("d1"));
        assertTrue(aliases.contains("d2"));
    }

    @Test
    void testSettersAndGetters() {
        // Test description setter/getter
        cmd.setDescription("Desc");
        assertEquals("Desc", cmd.getDescription());

        // Test usage setter/getter
        cmd.setUsage("/dummy usage");
        assertEquals("/dummy usage", cmd.getUsage());

        // Test permission setter/getter
        cmd.setPermission("perm.test");
        assertEquals("perm.test", cmd.getPermission());

        // Test gameOnly flag
        cmd.setGameOnly(true);
        assertTrue(cmd.inGameOnly());
        cmd.setGameOnly(false);
        assertFalse(cmd.inGameOnly());
    }

    @Test
    void testAddSubCommandAndIsSubcommandFlag() {
        DummyCommand sub = new DummyCommand();
        cmd.addSubCommand(sub);
        List<Command<String, Object>> subs = cmd.getSubcommands();
        assertEquals(1, subs.size());
        assertTrue(subs.contains(sub));
        assertTrue(sub.isSubCommand());
    }

    @Test
    void testRegisterDelegatesToManager() {
        AtomicBoolean called = new AtomicBoolean(false);
        CommandManager<String, Object> fakeManager = new CommandManager<>(new CommandPlatform<String, Object>() {
            @Override
            public String getPlugin() {
                return null;
            }

            @Override
            public void injectManager(CommandManager<String, Object> commandManager) {
            }

            @Override
            public java.util.logging.Logger getLogger() {
                return java.util.logging.Logger.getAnonymousLogger();
            }

            @Override
            public boolean hasPermission(Object sender, String permission) {
                return true;
            }

            @Override
            public boolean isPlayer(Object sender) {
                return false;
            }

            @Override
            public void sendMessage(Object sender, String message) {
            }

            @Override
            public void addCommand(Command<String, Object> command, String label) {
                called.set(true);
            }

            @Override
            public void removeCommand(String label, boolean subcommand) {
            }
        }) {
        };
        cmd.setManager(fakeManager);
        fakeManager.registerCommand(cmd);
        assertTrue(called.get());
    }

    @Test
    void testUnregisterDelegatesToManager() {
        AtomicBoolean called = new AtomicBoolean(false);
        CommandManager<String, Object> fakeManager = new CommandManager<String, Object>(new CommandPlatform<String, Object>() {
            @Override public String getPlugin() { return null; }
            @Override public void injectManager(CommandManager<String, Object> commandManager) {}
            @Override public java.util.logging.Logger getLogger() { return java.util.logging.Logger.getAnonymousLogger(); }
            @Override public boolean hasPermission(Object sender, String permission) { return true; }
            @Override public boolean isPlayer(Object sender) {return false;}
            @Override public void sendMessage(Object sender, String message) {}
            @Override public void addCommand(Command<String, Object> command, String label) {}
            @Override public void removeCommand(String label, boolean subcommand) { called.set(true); }
        }) {};
        cmd.setManager(fakeManager);
        cmd.unregister();
        assertTrue(called.get());
    }

    @Test
    void usage_noSubs_noArgs() {
        String usage = cmd.generateDefaultUsage(null, "dummy");
        assertEquals("/dummy", usage);
    }

    @Test
    void usage_onlyRequiredArgs() {
        cmd.addArgs("arg1", String.class);
        cmd.addArgs("arg2", Integer.class);
        String usage = cmd.generateDefaultUsage(null, "dummy");
        assertTrue(usage.startsWith("/dummy <arg1:string> <arg2:integer>"));
    }

    @Test
    void usage_requiredAndOptionalArgs() {
        cmd.addArgs("arg", String.class);
        cmd.addOptionalArgs("opt", Double.class);
        String usage = cmd.generateDefaultUsage(null, "dummy");
        System.out.println(usage);
        assertTrue(usage.contains("<arg:string>"));
        assertTrue(usage.contains("[opt:double]"));
    }

    @Test
    void usage_withSubcommands() {
        DummyCommand subA = new DummyCommand("suba");
        DummyCommand subB = new DummyCommand("subb");
        cmd.addSubCommand(subA, subB);
        String usage = cmd.generateDefaultUsage(null, "dummy");
        // extract first angle bracket content
        String inside = usage.substring(usage.indexOf('<')+1, usage.indexOf('>'));
        List<String> parts = Arrays.asList(inside.split("\\|"));
        assertTrue(parts.contains("suba"));
        assertTrue(parts.contains("subb"));
    }

    @Test
    void usage_subsAndArgsCombined() {
        DummyCommand subX = new DummyCommand("x");
        DummyCommand subY = new DummyCommand("y");
        cmd.addSubCommand(subX, subY);
        cmd.addArgs("req", String.class);
        cmd.addOptionalArgs("opt", String.class);

        String usage = cmd.generateDefaultUsage(null, "dummy");
        // expect "/dummy <x|y> <req:string> [opt:string]"
        assertTrue(usage.startsWith("/dummy <x|y>"));
        assertTrue(usage.contains("<req:string>"));
        assertTrue(usage.contains("[opt:string]"));
    }
}
