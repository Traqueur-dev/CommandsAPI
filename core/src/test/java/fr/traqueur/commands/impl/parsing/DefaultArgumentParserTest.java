package fr.traqueur.commands.impl.parsing;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.Infinite;
import fr.traqueur.commands.api.logging.Logger;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.parsing.ParseError;
import fr.traqueur.commands.api.parsing.ParseResult;
import fr.traqueur.commands.impl.logging.InternalLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultArgumentParserTest {

    private DefaultArgumentParser<Object, Object> parser;

    @BeforeEach
    void setUp() {
        Map<String, ArgumentConverter.Wrapper<?>> converters = new HashMap<>();
        converters.put("string", new ArgumentConverter.Wrapper<>(String.class, s -> s));
        converters.put("integer", new ArgumentConverter.Wrapper<>(Integer.class, s -> {
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }));
        converters.put("double", new ArgumentConverter.Wrapper<>(Double.class, s -> {
            try {
                return Double.valueOf(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }));

        Logger logger = new InternalLogger(java.util.logging.Logger.getLogger("ParserTest"));
        parser = new DefaultArgumentParser<>(converters, logger);
    }

    // --- Required args ---

    @Test
    void parse_requiredArgs_success() {
        Command<Object, Object> cmd = new DummyCommand();
        cmd.addArg("name", String.class);
        cmd.addArg("count", Integer.class);

        ParseResult result = parser.parse(cmd, new String[]{"hello", "42"});

        assertTrue(result.isSuccess());
        assertFalse(result.isError());
        assertEquals(2, result.consumedCount());

        Arguments args = result.arguments();
        assertEquals("hello", args.get("name"));
        assertEquals(42, (int) args.get("count"));
    }

    @Test
    void parse_missingRequiredArg_returnsError() {
        Command<Object, Object> cmd = new DummyCommand();
        cmd.addArg("name", String.class);
        cmd.addArg("count", Integer.class);

        ParseResult result = parser.parse(cmd, new String[]{"hello"});

        assertTrue(result.isError());
        assertFalse(result.isSuccess());
        assertEquals(ParseError.Type.MISSING_REQUIRED, result.error().type());
        assertEquals("count", result.error().argumentName());
    }

    // --- Optional args ---

    @Test
    void parse_optionalArgs_allProvided() {
        Command<Object, Object> cmd = new DummyCommand();
        cmd.addArg("req", String.class);
        cmd.addOptionalArg("opt1", Integer.class);
        cmd.addOptionalArg("opt2", Double.class);

        ParseResult result = parser.parse(cmd, new String[]{"value", "10", "3.14"});

        assertTrue(result.isSuccess());
        assertEquals(3, result.consumedCount());

        Arguments args = result.arguments();
        assertEquals("value", args.get("req"));
        assertEquals(10, (int) args.get("opt1"));
        assertEquals(3.14, args.<Double>get("opt2"), 0.001);
    }

    @Test
    void parse_optionalArgs_partiallyProvided() {
        Command<Object, Object> cmd = new DummyCommand();
        cmd.addArg("req", String.class);
        cmd.addOptionalArg("opt1", Integer.class);
        cmd.addOptionalArg("opt2", Double.class);

        ParseResult result = parser.parse(cmd, new String[]{"value", "10"});

        assertTrue(result.isSuccess());
        assertEquals(2, result.consumedCount());

        Arguments args = result.arguments();
        assertEquals("value", args.get("req"));
        assertEquals(10, (int) args.get("opt1"));
        assertFalse(args.has("opt2"));
    }

    @Test
    void parse_optionalArgs_noneProvided() {
        Command<Object, Object> cmd = new DummyCommand();
        cmd.addArg("req", String.class);
        cmd.addOptionalArg("opt", Integer.class);

        ParseResult result = parser.parse(cmd, new String[]{"value"});

        assertTrue(result.isSuccess());
        assertEquals(1, result.consumedCount());

        Arguments args = result.arguments();
        assertEquals("value", args.get("req"));
        assertFalse(args.has("opt"));
    }

    // --- Infinite args ---

    @Test
    void parse_infiniteArg_joinsRemaining() {
        Command<Object, Object> cmd = new DummyCommand();
        cmd.addArg("prefix", String.class);
        cmd.addArg("message", Infinite.class);

        ParseResult result = parser.parse(cmd, new String[]{"hello", "this", "is", "a", "message"});

        assertTrue(result.isSuccess());

        Arguments args = result.arguments();
        assertEquals("hello", args.get("prefix"));
        assertEquals("this is a message", args.get("message"));
    }

    @Test
    void parse_infiniteArg_empty() {
        Command<Object, Object> cmd = new DummyCommand();
        cmd.addArg("message", Infinite.class);

        ParseResult result = parser.parse(cmd, new String[]{});

        assertTrue(result.isSuccess());

        Arguments args = result.arguments();
        assertEquals("", args.get("message"));
    }

    @Test
    void parse_optionalInfiniteArg() {
        Command<Object, Object> cmd = new DummyCommand();
        cmd.addArg("prefix", String.class);
        cmd.addOptionalArg("rest", Infinite.class);

        ParseResult result = parser.parse(cmd, new String[]{"hello", "world", "!"});

        assertTrue(result.isSuccess());

        Arguments args = result.arguments();
        assertEquals("hello", args.get("prefix"));
        assertEquals("world !", args.get("rest"));
    }

    // --- Error cases ---

    @Test
    void parse_typeNotFound_returnsError() {
        // Register command with unknown type
        Command<Object, Object> cmd = new DummyCommand();
        cmd.addArg("unknown", UnknownType.class);

        ParseResult result = parser.parse(cmd, new String[]{"value"});

        assertTrue(result.isError());
        assertEquals(ParseError.Type.TYPE_NOT_FOUND, result.error().type());
    }

    @Test
    void parse_conversionFailed_returnsError() {
        Command<Object, Object> cmd = new DummyCommand();
        cmd.addArg("number", Integer.class);

        ParseResult result = parser.parse(cmd, new String[]{"notAnInteger"});

        assertTrue(result.isError());
        assertEquals(ParseError.Type.CONVERSION_FAILED, result.error().type());
        assertEquals("number", result.error().argumentName());
        assertEquals("notAnInteger", result.error().input());
    }

    @Test
    void parse_noArgs_success() {
        Command<Object, Object> cmd = new DummyCommand();

        ParseResult result = parser.parse(cmd, new String[]{});

        assertTrue(result.isSuccess());
        assertEquals(0, result.consumedCount());
        assertTrue(result.arguments().isEmpty());
    }

    // --- Helper classes ---

    private static class DummyCommand extends Command<Object, Object> {
        DummyCommand() {
            super(null, "dummy");
        }

        @Override
        public void execute(Object sender, Arguments arguments) {
        }
    }

    private static class UnknownType {
    }
}