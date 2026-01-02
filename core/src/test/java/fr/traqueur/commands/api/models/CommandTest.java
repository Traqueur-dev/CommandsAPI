package fr.traqueur.commands.api.models;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.test.mocks.MockCommandManager;
import fr.traqueur.commands.test.mocks.MockSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    private DummyCommand cmd;
    private MockCommandManager manager;

    @BeforeEach
    void setUp() {
        manager = new MockCommandManager();
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
        List<Command<Object, MockSender>> subs = cmd.getSubcommands();
        assertEquals(1, subs.size());
        assertTrue(subs.contains(sub));
        assertTrue(sub.isSubCommand());
    }

    @Test
    void testRegisterDelegatesToManager() {
        manager.registerCommand(cmd);
        assertTrue(manager.getMockPlatform().hasCommand("dummy"));
    }

    @Test
    void testUnregisterDelegatesToManager() {
        manager.registerCommand(cmd);
        assertTrue(manager.getMockPlatform().hasCommand("dummy"));
        cmd.unregister();
        assertFalse(manager.getMockPlatform().hasCommand("dummy"));
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
        String inside = usage.substring(usage.indexOf('<') + 1, usage.indexOf('>'));
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

    @Test
    void setEnabled_defaultIsTrue() {
        assertTrue(cmd.isEnabled());
    }

    // --- v5.0.0 new tests ---

    @Test
    void setEnabled_canDisable() {
        cmd.setEnabled(false);
        assertFalse(cmd.isEnabled());
    }

    @Test
    void setEnabled_canReEnable() {
        cmd.setEnabled(false);
        assertFalse(cmd.isEnabled());

        cmd.setEnabled(true);
        assertTrue(cmd.isEnabled());
    }

    @Test
    void getAllLabels_returnsNameOnly_whenNoAliases() {
        List<String> labels = cmd.getAllLabels();

        assertEquals(1, labels.size());
        assertEquals("dummy", labels.get(0));
    }

    @Test
    void getAllLabels_returnsNameAndAliases() {
        cmd.addAlias("d1", "d2", "d3");

        List<String> labels = cmd.getAllLabels();

        assertEquals(4, labels.size());
        assertEquals("dummy", labels.getFirst()); // Name first
        assertTrue(labels.contains("d1"));
        assertTrue(labels.contains("d2"));
        assertTrue(labels.contains("d3"));
    }

    @Test
    void getAliases_doesNotIncludeName() {
        cmd.addAlias("alias1");

        List<String> aliases = cmd.getAliases();

        assertEquals(1, aliases.size());
        assertFalse(aliases.contains("dummy"));
        assertTrue(aliases.contains("alias1"));
    }

    @Test
    void getAllLabels_nameAlwaysFirst() {
        cmd.addAlias("aaa"); // alphabetically before "dummy"

        List<String> labels = cmd.getAllLabels();

        assertEquals("dummy", labels.get(0));
    }

    private static class DummyCommand extends Command<Object, MockSender> {
        DummyCommand(String name) {
            super(null, name);
        }

        DummyCommand() {
            super(null, "dummy");
        }

        @Override
        public void execute(MockSender sender, Arguments arguments) {
            // no-op
        }
    }
}