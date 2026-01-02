package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.Infinite;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.ArgumentNotExistException;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.impl.logging.InternalLogger;
import fr.traqueur.commands.test.mocks.MockCommandManager;
import fr.traqueur.commands.test.mocks.MockPlatform;
import fr.traqueur.commands.test.mocks.MockSender;
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
    private MockCommandManager manager;
    private MockPlatform platform;

    @BeforeEach
    void setUp() {
        manager = new MockCommandManager();
        platform = manager.getMockPlatform();
        logger = new FakeLogger();
        manager.setLogger(logger);
    }

    // ----- TESTS -----
    @Test
    void testInfiniteArgsParsing() throws Exception {
        Command<Object, MockSender> cmd = new Command<>(null, "test") {
            @Override
            public void execute(MockSender sender, Arguments arguments) {
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
        Command<Object, MockSender> cmd = new DummyCommand();
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
        Command<Object, MockSender> cmd = new DummyCommand();
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
        Command<Object, MockSender> cmd = new DummyCommand();
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
        Command<Object, MockSender> cmd = new DummyCommand();
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
        Command<Object, MockSender> cmd = new DummyCommand();
        assertThrows(IllegalArgumentException.class, () -> cmd.addArgs("bad"));
    }

    @Test
    void testArgumentIncorrectException_onBadType() {
        Command<Object, MockSender> cmd = new DummyCommand();
        cmd.addArgs("n", Integer.class);
        String[] input = {"notAnInt"};
        assertThrows(ArgumentIncorrectException.class, () -> manager.parse(cmd, input));
    }

    @Test
    void testCommandRegistration_entriesInTree() {
        Command<Object, MockSender> cmd = new DummyCommand("main");
        cmd.addAlias("m");
        cmd.addSubCommand(new DummyCommand("sub"));

        manager.registerCommand(cmd);
        CommandTree<Object, MockSender> tree = manager.getCommands();
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
        List<String> added = platform.getRegisteredLabels();
        assertTrue(added.contains("dummy"));
        assertTrue(added.contains("a1"));
        assertTrue(added.contains("a2"));
        assertTrue(added.stream().anyMatch(label -> label.startsWith("dummy.")));
    }

    @Test
    void addCommand_shouldRegisterCompletersForArgs() {
        Command<Object, MockSender> cmd = new DummyCommand();
        cmd.addArgs("intArg", Integer.class);
        cmd.addOptionalArgs("optArg", Double.class);
        manager.registerCommand(cmd);

        Map<String, Map<Integer, TabCompleter<MockSender>>> comps = manager.getCompleters();
        assertTrue(comps.containsKey("dummy"));
        Map<Integer, TabCompleter<MockSender>> map = comps.get("dummy");
        assertTrue(map.containsKey(1));
        assertTrue(map.containsKey(2));
    }

    static class DummyCommand extends Command<Object, MockSender> {
        DummyCommand() {
            super(null, "dummy");
        }

        DummyCommand(String name) {
            super(null, name);
        }

        @Override
        public void execute(MockSender sender, Arguments args) {
        }
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
