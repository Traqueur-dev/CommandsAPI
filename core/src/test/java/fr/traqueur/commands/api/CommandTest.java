// Placez ce fichier sous core/src/test/java/fr/traqueur/commands/api/

package fr.traqueur.commands.api;

import fr.traqueur.commands.api.requirements.Requirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    private static class DummyCommand extends Command<String, Object> {
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
        cmd = new DummyCommand();
    }

    @Test
    void testAliasesAndName() {
        assertEquals("dummy", cmd.getName());
        assertTrue(cmd.getAliases().isEmpty());
        cmd.addAlias("d1", "d2");
        List<String> aliases = cmd.getAliases();
        assertEquals(2, aliases.size());
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
        CommandManager<String, Object> fakeManager = new CommandManager<String, Object>(new CommandPlatform<String, Object>() {
            @Override public String getPlugin() { return null; }
            @Override public void injectManager(CommandManager<String, Object> commandManager) {}
            @Override public java.util.logging.Logger getLogger() { return java.util.logging.Logger.getAnonymousLogger(); }
            @Override public boolean hasPermission(Object sender, String permission) { return true; }
            @Override public void addCommand(Command<String, Object> command, String label) {called.set(true);}
            @Override public void removeCommand(String label, boolean subcommand) {  }
        }) {};
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
            @Override public void addCommand(Command<String, Object> command, String label) {}
            @Override public void removeCommand(String label, boolean subcommand) { called.set(true); }
        }) {};
        cmd.setManager(fakeManager);
        cmd.unregister();
        assertTrue(called.get());
    }

    @Test
    void testAddArgsAndOptionalArgs() {
        // add required args
        cmd.addArgs("arg1", String.class);
        cmd.addArgs("arg2"); // string type
        assertEquals(2, cmd.getArgs().size());
        assertEquals("arg1:string", cmd.getArgs().get(0).arg().toLowerCase());
        assertEquals("arg2:string", cmd.getArgs().get(1).arg().toLowerCase());
        
        // add optional args
        cmd.addOptionalArgs("opt1", Integer.class);
        cmd.addOptionalArgs("opt2");
        assertEquals(2, cmd.getOptinalArgs().size());
        assertEquals("opt1:integer", cmd.getOptinalArgs().get(0).arg().toLowerCase());
        assertEquals("opt2:string", cmd.getOptinalArgs().get(1).arg().toLowerCase());
    }

    @Test
    void testGenerateDefaultUsage_noSubs_noArgs() {
        // when no subcommands or args, just /dummy
        String usage = cmd.generateDefaultUsage(null, null, "dummy");
        assertEquals("/dummy ", usage);
    }

    @Test
    void testGenerateDefaultUsage_withSubsAndArgs() {
        // prepare subcommands
        DummyCommand subA = new DummyCommand();
        subA.setUsage("/dummy suba");
        DummyCommand subB = new DummyCommand();
        cmd.addSubCommand(subA, subB);
        // prepare args
        cmd.addArgs("x", Integer.class);
        cmd.addOptionalArgs("y", String.class);

        // simulate platform and sender stub
        CommandPlatform<String, Object> platform = new CommandPlatform<String, Object>() {
            @Override public String getPlugin() { return null; }
            @Override public void injectManager(CommandManager<String, Object> commandManager) {}
            @Override public java.util.logging.Logger getLogger() { return java.util.logging.Logger.getAnonymousLogger(); }
            @Override public boolean hasPermission(Object sender, String permission) { return true; }
            @Override public void addCommand(Command<String, Object> command, String label) {}
            @Override public void removeCommand(String label, boolean subcommand) {}
        };
        String gen = cmd.generateDefaultUsage(platform, null, "dummy");
        // expected: /dummy <dummy|dummy> <x> [y]
        assertTrue(gen.startsWith("/dummy <"));
        assertTrue(gen.contains("<x:integer>"));
        assertTrue(gen.contains("[y:string]"));
    }
}
