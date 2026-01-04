package fr.traqueur.commands.annotations;

import fr.traqueur.commands.annotations.commands.*;
import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.test.mocks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnnotationCommandProcessor")
class AnnotationCommandProcessorTest {

    private MockPlatform platform;
    private AnnotationCommandProcessor<Object, MockSender> processor;

    @BeforeEach
    void setUp() {
        platform = new MockPlatform();
        MockCommandManager manager = new MockCommandManager(platform);
        processor = new AnnotationCommandProcessor<>(manager);
    }

    @Nested
    @DisplayName("Basic Registration")
    class BasicRegistration {

        @Test
        @DisplayName("should register simple command")
        void shouldRegisterSimpleCommand() {
            SimpleTestCommands commands = new SimpleTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("test"));
            Command<Object, MockSender> cmd = platform.getCommand("test");
            assertNotNull(cmd);
            assertEquals("A test command", cmd.getDescription());
            assertEquals("test.use", cmd.getPermission());
        }

        @Test
        @DisplayName("should register command with string argument")
        void shouldRegisterCommandWithStringArg() {
            SimpleTestCommands commands = new SimpleTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("greet"));
            Command<Object, MockSender> cmd = platform.getCommand("greet");
            
            List<Argument<MockSender>> args = cmd.getArgs();
            assertEquals(1, args.size());
            assertEquals("name", args.get(0).name());
        }

        @Test
        @DisplayName("should register command with multiple arguments")
        void shouldRegisterCommandWithMultipleArgs() {
            SimpleTestCommands commands = new SimpleTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("add"));
            Command<Object, MockSender> cmd = platform.getCommand("add");
            
            List<Argument<MockSender>> args = cmd.getArgs();
            assertEquals(2, args.size());
            assertEquals("a", args.get(0).name());
            assertEquals("b", args.get(1).name());
        }

        @Test
        @DisplayName("should register multiple handlers")
        void shouldRegisterMultipleHandlers() {
            SimpleTestCommands simple = new SimpleTestCommands();
            AliasTestCommands alias = new AliasTestCommands();
            
            processor.register(simple, alias);

            assertTrue(platform.hasCommand("test"));
            assertTrue(platform.hasCommand("greet"));
            assertTrue(platform.hasCommand("gamemode"));
            assertTrue(platform.hasCommand("teleport"));
        }
    }

    @Nested
    @DisplayName("Hierarchical Commands")
    class HierarchicalCommands {

        @Test
        @DisplayName("should register parent command")
        void shouldRegisterParentCommand() {
            HierarchicalTestCommands commands = new HierarchicalTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("admin"));
        }

        @Test
        @DisplayName("should register first level subcommands")
        void shouldRegisterFirstLevelSubcommands() {
            HierarchicalTestCommands commands = new HierarchicalTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("admin.reload"));
            assertTrue(platform.hasCommand("admin.info"));
        }

        @Test
        @DisplayName("should register second level subcommands")
        void shouldRegisterSecondLevelSubcommands() {
            HierarchicalTestCommands commands = new HierarchicalTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("admin.reload.config"));
            assertTrue(platform.hasCommand("admin.reload.plugins"));
        }

        @Test
        @DisplayName("should add subcommands to parent")
        void shouldAddSubcommandsToParent() {
            HierarchicalTestCommands commands = new HierarchicalTestCommands();
            processor.register(commands);

            Command<Object, MockSender> admin = platform.getCommand("admin");
            List<Command<Object, MockSender>> subcommands = admin.getSubcommands();
            assertEquals(2, subcommands.size());
            
            Command<Object, MockSender> reload = subcommands.stream()
                .filter(c -> c.getName().equals("reload"))
                .findFirst()
                .orElse(null);
            assertNotNull(reload);
            assertEquals(2, reload.getSubcommands().size());
        }

        @Test
        @DisplayName("should preserve permission on subcommands")
        void shouldPreservePermissionOnSubcommands() {
            HierarchicalTestCommands commands = new HierarchicalTestCommands();
            processor.register(commands);

            Command<Object, MockSender> admin = platform.getCommand("admin");
            Command<Object, MockSender> reload = admin.getSubcommands().stream()
                .filter(c -> c.getName().equals("reload"))
                .findFirst()
                .orElse(null);
            
            assertNotNull(reload);
            assertEquals("admin.reload", reload.getPermission());
        }
    }

    @Nested
    @DisplayName("Orphan Commands")
    class OrphanCommands {

        @Test
        @DisplayName("should register orphan subcommand directly")
        void shouldRegisterOrphanSubcommandDirectly() {
            OrphanTestCommands commands = new OrphanTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("warp.set"));
            assertTrue(platform.hasCommand("warp.delete"));
        }

        @Test
        @DisplayName("should register deep orphan command")
        void shouldRegisterDeepOrphanCommand() {
            OrphanTestCommands commands = new OrphanTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("config.database.reset"));
        }

        @Test
        @DisplayName("orphan commands should be gameOnly when using MockPlayer")
        void orphanCommandsShouldBeGameOnly() {
            OrphanTestCommands commands = new OrphanTestCommands();
            processor.register(commands);

            Command<Object, MockSender> warpSet = platform.getCommand("warp.set");
            assertTrue(warpSet.inGameOnly());
        }
    }

    @Nested
    @DisplayName("Optional Arguments")
    class OptionalArguments {

        @Test
        @DisplayName("should register command with optional argument")
        void shouldRegisterCommandWithOptionalArg() {
            OptionalArgsTestCommands commands = new OptionalArgsTestCommands();
            processor.register(commands);

            Command<Object, MockSender> heal = platform.getCommand("heal");

            assertEquals(0, heal.getArgs().size());
            assertEquals(1, heal.getOptionalArgs().size());
            assertEquals("target", heal.getOptionalArgs().get(0).name());
        }

        @Test
        @DisplayName("should register command with mixed required and optional args")
        void shouldRegisterMixedArgs() {
            OptionalArgsTestCommands commands = new OptionalArgsTestCommands();
            processor.register(commands);

            Command<Object, MockSender> give = platform.getCommand("give");

            assertEquals(1, give.getArgs().size());
            assertEquals("item", give.getArgs().getFirst().name());

            assertEquals(1, give.getOptionalArgs().size());
            assertEquals("amount", give.getOptionalArgs().getFirst().name());
        }
    }

    @Nested
    @DisplayName("Infinite Arguments")
    class InfiniteArguments {

        @Test
        @DisplayName("should register command with infinite argument")
        void shouldRegisterCommandWithInfiniteArg() {
            InfiniteArgsTestCommands commands = new InfiniteArgsTestCommands();
            processor.register(commands);

            Command<Object, MockSender> broadcast = platform.getCommand("broadcast");
            
            assertEquals(1, broadcast.getArgs().size());
            Argument<MockSender> arg = broadcast.getArgs().get(0);
            assertEquals("message", arg.name());
            assertTrue(arg.type().isInfinite());
        }

        @Test
        @DisplayName("should register command with optional infinite argument")
        void shouldRegisterOptionalInfiniteArg() {
            InfiniteArgsTestCommands commands = new InfiniteArgsTestCommands();
            processor.register(commands);

            Command<Object, MockSender> kick = platform.getCommand("kick");
            
            assertEquals(1, kick.getArgs().size());
            assertEquals(1, kick.getOptionalArgs().size());
            
            Argument<MockSender> reason = kick.getOptionalArgs().get(0);
            assertEquals("reason", reason.name());
            assertTrue(reason.type().isInfinite());
        }
    }

    @Nested
    @DisplayName("Aliases")
    class Aliases {

        @Test
        @DisplayName("should register command with single alias")
        void shouldRegisterSingleAlias() {
            AliasTestCommands commands = new AliasTestCommands();
            processor.register(commands);

            Command<Object, MockSender> gamemode = platform.getCommand("gamemode");
            
            List<String> aliases = gamemode.getAliases();
            assertEquals(1, aliases.size());
            assertTrue(aliases.contains("gm"));
        }

        @Test
        @DisplayName("should register command with multiple aliases")
        void shouldRegisterMultipleAliases() {
            AliasTestCommands commands = new AliasTestCommands();
            processor.register(commands);

            Command<Object, MockSender> teleport = platform.getCommand("teleport");
            
            List<String> aliases = teleport.getAliases();
            assertEquals(3, aliases.size());
            assertTrue(aliases.contains("tp"));
            assertTrue(aliases.contains("tpto"));
            assertTrue(aliases.contains("goto"));
        }

        @Test
        @DisplayName("all labels should be registered")
        void allLabelsShouldBeRegistered() {
            AliasTestCommands commands = new AliasTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("spawn"));
            assertTrue(platform.hasCommand("hub"));
            assertTrue(platform.hasCommand("lobby"));
            assertTrue(platform.hasCommand("s"));
        }
    }

    @Nested
    @DisplayName("Tab Completion")
    class TabCompletion {

        @Test
        @DisplayName("should register tab completer for argument")
        void shouldRegisterTabCompleter() {
            TabCompleteTestCommands commands = new TabCompleteTestCommands();
            processor.register(commands);

            Command<Object, MockSender> world = platform.getCommand("world");
            
            List<Argument<MockSender>> args = world.getArgs();
            assertEquals(1, args.size());
            assertNotNull(args.get(0).tabCompleter());
        }
    }

    @Nested
    @DisplayName("Game Only Detection")
    class GameOnlyDetection {

        @Test
        @DisplayName("should detect gameOnly when first param is MockPlayer")
        void shouldDetectGameOnlyWithMockPlayer() {
            AliasTestCommands commands = new AliasTestCommands();
            processor.register(commands);

            Command<Object, MockSender> gamemode = platform.getCommand("gamemode");
            assertTrue(gamemode.inGameOnly());
        }

        @Test
        @DisplayName("should not be gameOnly when first param is MockSender")
        void shouldNotBeGameOnlyWithMockSender() {
            SimpleTestCommands commands = new SimpleTestCommands();
            processor.register(commands);

            Command<Object, MockSender> test = platform.getCommand("test");
            assertFalse(test.inGameOnly());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("should throw when class is not annotated with @CommandContainer")
        void shouldThrowWhenMissingCommandContainer() {
            InvalidContainerNoAnnotation invalid = new InvalidContainerNoAnnotation();
            
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> processor.register(invalid)
            );
            
            assertTrue(ex.getMessage().contains("@CommandContainer"));
        }

        @Test
        @DisplayName("should throw when parameter is missing @Arg annotation")
        void shouldThrowWhenMissingArgAnnotation() {
            InvalidContainerMissingArg invalid = new InvalidContainerMissingArg();
            
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> processor.register(invalid)
            );
            
            assertTrue(ex.getMessage().contains("@Arg"));
        }
    }

    @Nested
    @DisplayName("Registration Order")
    class RegistrationOrder {

        @Test
        @DisplayName("commands should be sorted by depth (parents first)")
        void commandsShouldBeSortedByDepth() {
            HierarchicalTestCommands commands = new HierarchicalTestCommands();
            processor.register(commands);

            List<String> labels = platform.getRegisteredLabels();

            int adminIndex = labels.indexOf("admin");
            int adminReloadIndex = labels.indexOf("admin.reload");
            int adminReloadConfigIndex = labels.indexOf("admin.reload.config");

            assertTrue(adminIndex < adminReloadIndex,
                "admin should be registered before admin.reload");
            assertTrue(adminReloadIndex < adminReloadConfigIndex,
                "admin.reload should be registered before admin.reload.config");
        }
    }

    @Nested
    @DisplayName("Primitive Types Support")
    class PrimitiveTypesSupport {

        @Test
        @DisplayName("should register command with primitive int argument")
        void shouldRegisterCommandWithPrimitiveInt() {
            PrimitiveTypesTestCommands commands = new PrimitiveTypesTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("primitiveint"));
            Command<Object, MockSender> cmd = platform.getCommand("primitiveint");
            assertNotNull(cmd);

            List<Argument<MockSender>> args = cmd.getArgs();
            assertEquals(1, args.size());
            assertEquals("value", args.get(0).name());
        }

        @Test
        @DisplayName("should register command with primitive long argument")
        void shouldRegisterCommandWithPrimitiveLong() {
            PrimitiveTypesTestCommands commands = new PrimitiveTypesTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("primitivelong"));
            Command<Object, MockSender> cmd = platform.getCommand("primitivelong");
            assertNotNull(cmd);

            List<Argument<MockSender>> args = cmd.getArgs();
            assertEquals(1, args.size());
            assertEquals("value", args.get(0).name());
        }

        @Test
        @DisplayName("should register command with primitive double argument")
        void shouldRegisterCommandWithPrimitiveDouble() {
            PrimitiveTypesTestCommands commands = new PrimitiveTypesTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("primitivedouble"));
            Command<Object, MockSender> cmd = platform.getCommand("primitivedouble");
            assertNotNull(cmd);

            List<Argument<MockSender>> args = cmd.getArgs();
            assertEquals(1, args.size());
            assertEquals("value", args.get(0).name());
        }

        @Test
        @DisplayName("should register command with primitive boolean argument")
        void shouldRegisterCommandWithPrimitiveBoolean() {
            PrimitiveTypesTestCommands commands = new PrimitiveTypesTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("primitivebool"));
            Command<Object, MockSender> cmd = platform.getCommand("primitivebool");
            assertNotNull(cmd);

            List<Argument<MockSender>> args = cmd.getArgs();
            assertEquals(1, args.size());
            assertEquals("enabled", args.get(0).name());
        }

        @Test
        @DisplayName("should register command with mixed primitive types")
        void shouldRegisterCommandWithMixedPrimitives() {
            PrimitiveTypesTestCommands commands = new PrimitiveTypesTestCommands();
            processor.register(commands);

            assertTrue(platform.hasCommand("mixedprimitives"));
            Command<Object, MockSender> cmd = platform.getCommand("mixedprimitives");
            assertNotNull(cmd);

            List<Argument<MockSender>> args = cmd.getArgs();
            assertEquals(3, args.size());
            assertEquals("count", args.get(0).name());
            assertEquals("enabled", args.get(1).name());
            assertEquals("ratio", args.get(2).name());
        }
    }
}