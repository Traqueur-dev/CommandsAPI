package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.Infinite;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.ArgumentNotExistException;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandPlatform;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.impl.logging.InternalLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class CommandManagerTest {

    private FakeLogger logger;
    private CommandManager<Object, String> manager;
    private FakePlatform platform;

    @BeforeEach
    void setUp() {
        platform = new FakePlatform();
        manager = new CommandManager<>(platform) {
        };
        platform.injectManager(manager);
        logger = new FakeLogger();
        manager.setLogger(logger);
    }

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

    // ----- TESTS -----
    @Test
    void testInfiniteArgsParsing() throws Exception {
        Command<Object, String> cmd = new Command<>(null, "test") {
            @Override
            public void execute(String sender, Arguments arguments) {
            }
        };
        cmd.setManager(manager);
        cmd.addArgs("rest", Infinite.class);

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
        cmd.addArgs("rest", Infinite.class);

        String[] input = {"A", "B", "C", "D"};
        Arguments args = manager.parse(cmd, input);

        assertEquals("A", args.get("first"));
        assertEquals("B C D", args.get("rest"));
    }

    @Test
    void testNoExtraAfterInfinite() throws Exception {
        Command<Object, String> cmd = new DummyCommand();
        cmd.setManager(manager);
        cmd.addArg("x", Infinite.class);
        cmd.addArg("y", String.class);

        String[] input = {"v1", "v2"};
        Arguments args = manager.parse(cmd, input);
        assertEquals("v1 v2", args.get("x"));
        assertThrows(ArgumentNotExistException.class, () -> args.get("y"));
    }

    @Test
    void testOptionalArgs_onlyDefault() throws Exception {
        Command<Object, String> cmd = new DummyCommand();
        cmd.addArgs("req", String.class);
        cmd.addOptionalArgs("opt1", Integer.class);
        cmd.addOptionalArgs("opt2", Double.class);

        String[] input = {"reqValue"};
        Arguments args = manager.parse(cmd, input);
        assertEquals("reqValue", args.get("req"));

        assertFalse(args.getOptional("opt1").isPresent());
        assertFalse(args.getOptional("opt2").isPresent());
        assertEquals(0, args.<Integer>getOptional("opt1").orElse(0));
        assertEquals(0.0, args.<Double>getOptional("opt2").orElse(0.0));
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
    void addArgs_withOddArgs_shouldThrow() {
        Command<Object, String> cmd = new DummyCommand();
        assertThrows(IllegalArgumentException.class, () -> cmd.addArgs("bad"));
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

    static class FakeLogger extends InternalLogger {
        private final List<String> errors = new ArrayList<>();

        public FakeLogger() {
            super(Logger.getLogger("FakeLogger"));
        }

        @Override
        public void error(String message) {
            errors.add(message);
        }

        public List<String> getErrors() {
            return errors;
        }
    }

}
