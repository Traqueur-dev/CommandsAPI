package fr.traqueur.commands.api.models;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.requirements.Requirement;
import fr.traqueur.commands.test.mocks.MockCommandManager;
import fr.traqueur.commands.test.mocks.MockPlatform;
import fr.traqueur.commands.test.mocks.MockSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CommandBuilderTest {

    private MockCommandManager manager;
    private MockPlatform platform;

    @BeforeEach
    void setUp() {
        manager = new MockCommandManager();
        platform = manager.getMockPlatform();
    }

    // --- Basic building ---

    @Test
    void build_simpleCommand_success() {
        Command<Object, MockSender> cmd = manager.command("test")
                .description("Test description")
                .usage("/test")
                .permission("test.use")
                .executor((sender, args) -> {
                })
                .build();

        assertEquals("test", cmd.getName());
        assertEquals("Test description", cmd.getDescription());
        assertEquals("/test", cmd.getUsage());
        assertEquals("test.use", cmd.getPermission());
    }

    @Test
    void build_withoutExecutor_throwsException() {
        CommandBuilder<Object, MockSender> builder = manager.command("test")
                .description("Test");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void build_withGameOnly_setsFlag() {
        Command<Object, MockSender> cmd = manager.command("test")
                .gameOnly()
                .executor((sender, args) -> {
                })
                .build();

        assertTrue(cmd.inGameOnly());
    }

    @Test
    void build_withGameOnlyFalse_clearsFlag() {
        Command<Object, MockSender> cmd = manager.command("test")
                .gameOnly(false)
                .executor((sender, args) -> {
                })
                .build();

        assertFalse(cmd.inGameOnly());
    }

    // --- Arguments ---

    @Test
    void build_withArgs_addsArguments() {
        Command<Object, MockSender> cmd = manager.command("test")
                .arg("name", String.class)
                .arg("count", Integer.class)
                .executor((sender, args) -> {
                })
                .build();

        assertEquals(2, cmd.getArgs().size());
        assertEquals("name", cmd.getArgs().get(0).name());
        assertEquals("count", cmd.getArgs().get(1).name());
    }

    @Test
    void build_withOptionalArgs_addsOptionalArguments() {
        Command<Object, MockSender> cmd = manager.command("test")
                .arg("required", String.class)
                .optionalArg("optional1", Integer.class)
                .optionalArg("optional2", Double.class)
                .executor((sender, args) -> {
                })
                .build();

        assertEquals(1, cmd.getArgs().size());
        assertEquals(2, cmd.getOptionalArgs().size());
        assertEquals("optional1", cmd.getOptionalArgs().get(0).name());
        assertEquals("optional2", cmd.getOptionalArgs().get(1).name());
    }

    @Test
    void build_withTabCompleter_addsCustomCompleter() {
        Command<Object, MockSender> cmd = manager.command("test")
                .arg("player", String.class, (sender, args) -> List.of("Alice", "Bob"))
                .executor((sender, args) -> {
                })
                .build();

        assertNotNull(cmd.getArgs().getFirst().tabCompleter());
    }

    // --- Aliases ---

    @Test
    void build_withAlias_addsAlias() {
        Command<Object, MockSender> cmd = manager.command("test")
                .alias("t")
                .executor((sender, args) -> {
                })
                .build();

        assertEquals(1, cmd.getAliases().size());
        assertTrue(cmd.getAliases().contains("t"));
    }

    @Test
    void build_withAliases_addsMultipleAliases() {
        Command<Object, MockSender> cmd = manager.command("test")
                .aliases("t", "tst", "te")
                .executor((sender, args) -> {
                })
                .build();

        assertEquals(3, cmd.getAliases().size());
        assertTrue(cmd.getAliases().contains("t"));
        assertTrue(cmd.getAliases().contains("tst"));
        assertTrue(cmd.getAliases().contains("te"));
    }

    // --- Subcommands ---

    @Test
    void build_withSubcommand_addsSubcommand() {
        Command<Object, MockSender> sub = manager.command("sub")
                .executor((sender, args) -> {
                })
                .build();

        Command<Object, MockSender> cmd = manager.command("main")
                .subcommand(sub)
                .executor((sender, args) -> {
                })
                .build();

        assertEquals(1, cmd.getSubcommands().size());
        assertSame(sub, cmd.getSubcommands().get(0));
    }

    @Test
    void build_withSubcommands_addsMultipleSubcommands() {
        Command<Object, MockSender> sub1 = manager.command("sub1")
                .executor((sender, args) -> {
                })
                .build();

        Command<Object, MockSender> sub2 = manager.command("sub2")
                .executor((sender, args) -> {
                })
                .build();

        Command<Object, MockSender> cmd = manager.command("main")
                .subcommands(sub1, sub2)
                .executor((sender, args) -> {
                })
                .build();

        assertEquals(2, cmd.getSubcommands().size());
    }

    // --- Requirements ---

    @Test
    void build_withRequirement_addsRequirement() {
        Requirement<MockSender> req = new Requirement<MockSender>() {
            @Override
            public boolean check(MockSender sender) {
                return true;
            }

            @Override
            public String errorMessage() {
                return "Error";
            }
        };

        Command<Object, MockSender> cmd = manager.command("test")
                .requirement(req)
                .executor((sender, args) -> {
                })
                .build();

        assertEquals(1, cmd.getRequirements().size());
        assertSame(req, cmd.getRequirements().get(0));
    }

    @Test
    void build_withRequirements_addsMultipleRequirements() {
        Command<Object, MockSender> cmd = manager.command("test")
                .requirements(
                        new Requirement<MockSender>() {
                            @Override
                            public boolean check(MockSender sender) {
                                return true;
                            }

                            @Override
                            public String errorMessage() {
                                return "";
                            }
                        },
                        new Requirement<MockSender>() {
                            @Override
                            public boolean check(MockSender sender) {
                                return false;
                            }

                            @Override
                            public String errorMessage() {
                                return "";
                            }
                        }
                )
                .executor((sender, args) -> {
                })
                .build();

        assertEquals(2, cmd.getRequirements().size());
    }

    // --- Executor ---

    @Test
    void build_executorIsCalled() {
        AtomicReference<String> received = new AtomicReference<>();

        Command<Object, MockSender> cmd = manager.command("test")
                .executor((sender, args) -> {
                    received.set("executed");
                })
                .build();

        cmd.execute(null, new Arguments(new fr.traqueur.commands.impl.logging.InternalLogger(java.util.logging.Logger.getLogger("test"))));

        assertEquals("executed", received.get());
    }

    // --- Register ---

    @Test
    void register_registersCommandInManager() {
        Command<Object, MockSender> cmd = manager.command("registered")
                .executor((sender, args) -> {
                })
                .register();

        assertTrue(platform.getRegisteredLabels().contains("registered"));
        assertTrue(manager.getCommands().findNode("registered", new String[]{}).isPresent());
    }

    @Test
    void register_returnsBuiltCommand() {
        Command<Object, MockSender> cmd = manager.command("test")
                .description("Test")
                .executor((sender, args) -> {
                })
                .register();

        assertNotNull(cmd);
        assertEquals("test", cmd.getName());
        assertEquals("Test", cmd.getDescription());
    }

    // --- Fluent chain ---

    @Test
    void build_fluentChain_allOptions() {
        Command<Object, MockSender> sub = manager.command("sub")
                .executor((sender, args) -> {
                })
                .build();

        Command<Object, MockSender> cmd = manager.command("complex")
                .description("Complex command")
                .usage("/complex <arg> [opt]")
                .permission("complex.use")
                .gameOnly()
                .aliases("c", "cx")
                .arg("required", String.class)
                .optionalArg("optional", Integer.class)
                .subcommand(sub)
                .requirement(new Requirement<MockSender>() {
                    @Override
                    public boolean check(MockSender sender) {
                        return true;
                    }

                    @Override
                    public String errorMessage() {
                        return "";
                    }
                })
                .executor((sender, args) -> {
                })
                .build();

        assertEquals("complex", cmd.getName());
        assertEquals("Complex command", cmd.getDescription());
        assertEquals("/complex <arg> [opt]", cmd.getUsage());
        assertEquals("complex.use", cmd.getPermission());
        assertTrue(cmd.inGameOnly());
        assertEquals(2, cmd.getAliases().size());
        assertEquals(1, cmd.getArgs().size());
        assertEquals(1, cmd.getOptionalArgs().size());
        assertEquals(1, cmd.getSubcommands().size());
        assertEquals(1, cmd.getRequirements().size());
    }

    // --- Default values ---

    @Test
    void build_defaultValues_areEmpty() {
        Command<Object, MockSender> cmd = manager.command("minimal")
                .executor((sender, args) -> {
                })
                .build();

        assertEquals("minimal", cmd.getName());
        assertEquals("", cmd.getDescription());
        assertEquals("", cmd.getUsage());
        assertEquals("", cmd.getPermission());
        assertFalse(cmd.inGameOnly());
        assertTrue(cmd.getAliases().isEmpty());
        assertTrue(cmd.getArgs().isEmpty());
        assertTrue(cmd.getOptionalArgs().isEmpty());
        assertTrue(cmd.getSubcommands().isEmpty());
        assertTrue(cmd.getRequirements().isEmpty());
    }

    @Test
    void build_commandIsEnabled_byDefault() {
        Command<Object, MockSender> cmd = manager.command("test")
                .executor((sender, args) -> {
                })
                .build();

        assertTrue(cmd.isEnabled());
    }

}