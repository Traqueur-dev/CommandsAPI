package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandPlatform;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.impl.logging.InternalLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

class CommandManagerTest {

    private InternalLogger logger;
    private CommandManager<Object, String> manager;
    private FakePlatform platform;

    static class DummyCommand extends Command<Object, String> {
        DummyCommand() { super(null, "dummy"); }
        DummyCommand(String name) { super(null, name); }
        @Override public void execute(String sender, Arguments args) {}
    }

    static class FakePlatform implements CommandPlatform<Object, String> {
        List<String> added = new ArrayList<>();
        @Override public Object getPlugin() { return null; }
        @Override public void injectManager(CommandManager<Object, String> cm) {}
        @Override public java.util.logging.Logger getLogger() { return java.util.logging.Logger.getAnonymousLogger(); }
        @Override public boolean hasPermission(String sender, String permission) { return true; }
        @Override public boolean isPlayer(String sender) {return false;}
        @Override public void sendMessage(String sender, String message) {}
        @Override public void addCommand(Command<Object, String> command, String label) { added.add(label); }
        @Override public void removeCommand(String label, boolean sub) {}
    }

    @BeforeEach
    void setUp() {
        platform = new FakePlatform();
        manager = new CommandManager<Object, String>(platform) {};
        platform.injectManager(manager);
        logger = Mockito.mock(InternalLogger.class);
        manager.setLogger(logger);
    }

    @Test
    void testInfiniteArgsParsing() throws Exception {
        Command<Object, String> cmd = new Command<Object, String>(null, "test") {
            @Override
            public void execute(String sender, Arguments arguments) {}
        };
        cmd.setManager(manager);
        cmd.addArgs("rest:infinite");

        String[] input = {"one", "two", "three", "four"};
        Arguments args = manager.parse(cmd, input);
        Object rest = args.get("rest");
        assertInstanceOf(String.class, rest);
        assertNotNull(rest);
        assertEquals("one two three four", rest);
    }

    @Test
    void testInfiniteArgsStopsFurtherParsing() throws Exception {
        Command<Object, String> cmd = new DummyCommand();
        cmd.setManager(manager);
        cmd.addArgs("first", String.class);
        cmd.addArgs("rest:infinite");

        String[] input = {"A", "B", "C", "D"};
        Arguments args = manager.parse(cmd, input);

        assertEquals("A", args.getAsString("first", null));
        assertEquals("B C D", args.get("rest"));
    }

    @Test
    void testNoExtraAfterInfinite() throws Exception {
        Command<Object, String> cmd = new DummyCommand();
        cmd.setManager(manager);
        cmd.addArgs("x:infinite");
        cmd.addArgs("y", String.class);
        verify(logger).error("Arguments cannot follow infinite arguments.");

        String[] input = {"v1", "v2"};
        Arguments args = manager.parse(cmd, input);
        assertEquals("v1 v2", args.get("x"));
        assertNull(args.get("y"));
        verify(logger).error(contains("y"));
    }

    @Test
    void testBasicArgParsing_correctTypes() throws Exception {
        Command<Object, String> cmd = new DummyCommand();
        cmd.addArgs("num", Integer.class);
        cmd.addOptionalArgs("opt", String.class);

        String[] input = {"42", "hello"};
        Arguments args = manager.parse(cmd, input);

        int num = args.get("num");
        assertEquals(42, num);
        Optional<String> opt = args.getOptional("opt");
        assertTrue(opt.isPresent());
        assertEquals("hello", opt.get());
    }

    @Test
    void testOptionalArgs_onlyDefault() throws Exception {
        Command<Object, String> cmd = new DummyCommand();
        cmd.addArgs("req", String.class);
        cmd.addOptionalArgs("opt1", Integer.class);
        cmd.addOptionalArgs("opt2", Double.class);

        String[] input = {"reqValue"};
        Arguments args = manager.parse(cmd, input);
        assertEquals("reqValue", args.getAsString("req", null));

        assertFalse(args.getOptional("opt1").isPresent());
        assertFalse(args.getOptional("opt2").isPresent());
        assertEquals(0, args.getAsInt("opt1", 0));
        assertEquals(0.0, args.getAsDouble("opt2", 0.0));
    }

    @Test
    void testArgumentIncorrectException_onBadType() {
        Command<Object, String> cmd = new DummyCommand();
        cmd.addArgs("n", Integer.class);
        String[] input = {"notAnInt"};
        assertThrows(ArgumentIncorrectException.class, () -> manager.parse(cmd, input));
    }

    @Test
    void testCommandRegistration_entriesInTree() {
        Command<Object, String> cmd = new DummyCommand("main");
        cmd.addAlias("m");
        cmd.addSubCommand(new DummyCommand("sub"));

        manager.registerCommand(cmd);
        CommandTree<Object, String> tree = manager.getCommands();
        assertTrue(tree.getRoot().getChildren().containsKey("main"));
        assertTrue(tree.getRoot().getChildren().containsKey("m"));
        assertTrue(tree.findNode("main", new String[]{"sub"}).isPresent());
    }

    @Test
    void registerCommand_shouldAddMainAndAliasAndSubcommands() {
        DummyCommand main = new DummyCommand();
        main.addAlias("a1", "a2");
        DummyCommand sub = new DummyCommand();
        main.addSubCommand(sub);

        manager.registerCommand(main);
        List<String> added = platform.added;
        assertTrue(added.contains("dummy"));
        assertTrue(added.contains("a1"));
        assertTrue(added.contains("a2"));
        assertTrue(added.stream().anyMatch(label -> label.startsWith("dummy.")));
    }

    @Test
    void addCommand_shouldRegisterCompletersForArgs() {
        Command<Object, String> cmd = new DummyCommand();
        cmd.addArgs("intArg", Integer.class);
        cmd.addOptionalArgs("optArg", Double.class);
        manager.registerCommand(cmd);

        Map<String, Map<Integer, TabCompleter<String>>> comps = manager.getCompleters();
        assertTrue(comps.containsKey("dummy"));
        Map<Integer, TabCompleter<String>> map = comps.get("dummy");
        assertTrue(map.containsKey(1));
        assertTrue(map.containsKey(2));
    }

    @Test
    void addCommand_withUnknownType_shouldThrow() {
        Command<Object, String> cmd = new DummyCommand();
        cmd.addArgs("bad:typeparser");
        assertThrows(RuntimeException.class, () -> manager.registerCommand(cmd));
    }

}
