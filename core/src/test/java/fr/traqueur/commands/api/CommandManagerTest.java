package fr.traqueur.commands.api;

import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.impl.logging.InternalLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

class CommandManagerTest {

    private InternalLogger logger;
    private CommandManager<Object, String> manager;
    private static class FakePlatform implements CommandPlatform<Object, String> {
        @Override public Object getPlugin() { return null; }
        @Override public void injectManager(CommandManager<Object, String> commandManager) {}
        @Override public Logger getLogger() { return Logger.getAnonymousLogger(); }
        @Override public boolean hasPermission(String sender, String permission) { return true; }
        @Override public void addCommand(Command<Object, String> command, String label) {}
        @Override public void removeCommand(String label, boolean subcommand) {}
    }

    @BeforeEach
    void setUp() {
        FakePlatform platform = new FakePlatform();
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
        Command<Object, String> cmd = new Command<Object, String>(null, "cmd") {
            @Override public void execute(String sender, Arguments arguments) {}
        };
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
        // Vérifier que l'ajout d'un argument après un infini lève une exception gérée
        Command<Object, String> cmd = new Command<Object, String>(null, "err") {
            @Override public void execute(String sender, Arguments arguments) {}
        };
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
        Command<Object, String> cmd = new Command<Object, String>(null, "basic") {
            @Override public void execute(String sender, Arguments arguments) {}
        };
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
        Command<Object, String> cmd = new Command<Object, String>(null, "opt") {
            @Override public void execute(String sender, Arguments arguments) {}
        };
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
        Command<Object, String> cmd = new Command<Object, String>(null, "badtype") {
            @Override public void execute(String sender, Arguments arguments) {}
        };
        cmd.addArgs("n", Integer.class);
        String[] input = {"notAnInt"};
        assertThrows(ArgumentIncorrectException.class, () -> manager.parse(cmd, input));
    }

    @Test
    void testCommandRegistration_entriesInManager() {
        Command<Object, String> cmd = new Command<Object, String>(null, "main") {
            @Override public void execute(String sender, Arguments arguments) {}
        };
        cmd.addAlias("m");
        cmd.addSubCommand(new Command<Object, String>(null, "sub") {
            @Override public void execute(String sender, Arguments arguments) {}
        });

        manager.registerCommand(cmd);
        Map<String, Command<Object, String>> map = manager.getCommands();
        assertTrue(map.containsKey("main"));
        assertTrue(map.containsKey("m"));
        assertTrue(map.containsKey("main.sub"));
    }

}
