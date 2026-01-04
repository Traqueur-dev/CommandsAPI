package fr.traqueur.commands.annotations;

import fr.traqueur.commands.annotations.commands.PrimitiveTypesTestCommands;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.test.mocks.MockCommandManager;
import fr.traqueur.commands.test.mocks.MockPlatform;
import fr.traqueur.commands.test.mocks.MockSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for primitive types support.
 * These tests verify that commands with primitive type arguments can be:
 * 1. Parsed correctly
 * 2. Stored in Arguments
 * 3. Retrieved from Arguments
 * 4. Passed to method invocation via reflection
 */
@DisplayName("Primitive Types Integration")
class PrimitiveTypesIntegrationTest {

    private MockPlatform platform;
    private MockCommandManager manager;
    private PrimitiveTypesTestCommands commands;

    // Mock sender implementation for testing
    private static class TestSender implements MockSender {
        @Override
        public void sendMessage(String message) {
            // Do nothing
        }

        @Override
        public boolean hasPermission(String permission) {
            return true;
        }
    }

    @BeforeEach
    void setUp() {
        platform = new MockPlatform();
        manager = new MockCommandManager(platform);
        AnnotationCommandProcessor<Object, MockSender> processor = new AnnotationCommandProcessor<>(manager);
        commands = new PrimitiveTypesTestCommands();
        processor.register(commands);
    }

    @Test
    @DisplayName("should execute command with primitive int")
    void shouldExecuteCommandWithPrimitiveInt() throws Exception {
        Command<Object, MockSender> cmd = platform.getCommand("primitiveint");
        assertNotNull(cmd);

        // Parse arguments
        Arguments args = manager.parse(cmd, new String[]{"42"});
        assertNotNull(args);
        assertEquals(1, args.size());

        // Verify the argument was parsed and stored correctly
        Integer value = args.get("value");
        assertEquals(42, value);

        // Execute the command
        TestSender sender = new TestSender();
        cmd.execute(sender, args);

        // Verify the method was called
        assertEquals(1, commands.executedCommands.size());
        assertEquals("primitiveint", commands.executedCommands.getFirst());

        // Verify arguments were passed correctly
        Object[] invokeArgs = commands.executedArgs.getFirst();
        assertEquals(2, invokeArgs.length);
        assertEquals(sender, invokeArgs[0]);
        assertEquals(42, invokeArgs[1]);
        assertInstanceOf(Integer.class, invokeArgs[1]);
    }

    @Test
    @DisplayName("should execute command with primitive boolean")
    void shouldExecuteCommandWithPrimitiveBoolean() throws Exception {
        Command<Object, MockSender> cmd = platform.getCommand("primitivebool");
        assertNotNull(cmd);

        // Parse arguments
        Arguments args = manager.parse(cmd, new String[]{"true"});
        assertNotNull(args);

        // Verify the argument was parsed and stored correctly
        Boolean enabled = args.get("enabled");
        assertTrue(enabled);

        // Execute the command
        TestSender sender = new TestSender();
        cmd.execute(sender, args);

        // Verify the method was called correctly
        assertEquals(1, commands.executedCommands.size());
        Object[] invokeArgs = commands.executedArgs.getFirst();
        assertEquals(true, invokeArgs[1]);
        assertInstanceOf(Boolean.class, invokeArgs[1]);
    }

    @Test
    @DisplayName("should execute command with primitive double")
    void shouldExecuteCommandWithPrimitiveDouble() throws Exception {
        Command<Object, MockSender> cmd = platform.getCommand("primitivedouble");
        assertNotNull(cmd);

        // Parse arguments
        Arguments args = manager.parse(cmd, new String[]{"3.14"});
        assertNotNull(args);

        // Verify the argument was parsed and stored correctly
        Double value = args.get("value");
        assertEquals(3.14, value, 0.001);

        // Execute the command
        TestSender sender = new TestSender();
        cmd.execute(sender, args);

        // Verify the method was called correctly
        assertEquals(1, commands.executedCommands.size());
        Object[] invokeArgs = commands.executedArgs.getFirst();
        assertInstanceOf(Double.class, invokeArgs[1]);
        assertEquals(3.14, (Double) invokeArgs[1], 0.001);
    }

    @Test
    @DisplayName("should execute command with primitive long")
    void shouldExecuteCommandWithPrimitiveLong() throws Exception {
        Command<Object, MockSender> cmd = platform.getCommand("primitivelong");
        assertNotNull(cmd);

        // Parse arguments
        Arguments args = manager.parse(cmd, new String[]{"9223372036854775807"});
        assertNotNull(args);

        // Verify the argument was parsed and stored correctly
        Long value = args.get("value");
        assertEquals(9223372036854775807L, value);

        // Execute the command
        TestSender sender = new TestSender();
        cmd.execute(sender, args);

        // Verify the method was called correctly
        assertEquals(1, commands.executedCommands.size());
        Object[] invokeArgs = commands.executedArgs.getFirst();
        assertEquals(9223372036854775807L, invokeArgs[1]);
        assertInstanceOf(Long.class, invokeArgs[1]);
    }

    @Test
    @DisplayName("should execute command with mixed primitive types")
    void shouldExecuteCommandWithMixedPrimitives() throws Exception {
        Command<Object, MockSender> cmd = platform.getCommand("mixedprimitives");
        assertNotNull(cmd);

        // Parse arguments
        Arguments args = manager.parse(cmd, new String[]{"10", "true", "2.5"});
        assertNotNull(args);
        assertEquals(3, args.size());

        // Verify all arguments were parsed and stored correctly
        Integer count = args.get("count");
        Boolean enabled = args.get("enabled");
        Double ratio = args.get("ratio");

        assertEquals(10, count);
        assertTrue(enabled);
        assertEquals(2.5, ratio, 0.001);

        // Execute the command
        TestSender sender = new TestSender();
        cmd.execute(sender, args);

        // Verify the method was called correctly with all arguments
        assertEquals(1, commands.executedCommands.size());
        assertEquals("mixedprimitives", commands.executedCommands.getFirst());

        Object[] invokeArgs = commands.executedArgs.getFirst();
        assertEquals(4, invokeArgs.length);
        assertEquals(sender, invokeArgs[0]);
        assertEquals(10, invokeArgs[1]);
        assertEquals(true, invokeArgs[2]);
        assertEquals(2.5, (Double) invokeArgs[3], 0.001);
    }

    @Test
    @DisplayName("should handle negative primitive int")
    void shouldHandleNegativePrimitiveInt() throws Exception {
        Command<Object, MockSender> cmd = platform.getCommand("primitiveint");
        Arguments args = manager.parse(cmd, new String[]{"-42"});

        Integer value = args.get("value");
        assertEquals(-42, value);

        TestSender sender = new TestSender();
        cmd.execute(sender, args);

        Object[] invokeArgs = commands.executedArgs.getFirst();
        assertEquals(-42, invokeArgs[1]);
    }

    @Test
    @DisplayName("should handle false boolean primitive")
    void shouldHandleFalseBooleanPrimitive() throws Exception {
        Command<Object, MockSender> cmd = platform.getCommand("primitivebool");
        Arguments args = manager.parse(cmd, new String[]{"false"});

        Boolean enabled = args.get("enabled");
        assertFalse(enabled);

        TestSender sender = new TestSender();
        cmd.execute(sender, args);

        Object[] invokeArgs = commands.executedArgs.getFirst();
        assertEquals(false, invokeArgs[1]);
    }
}