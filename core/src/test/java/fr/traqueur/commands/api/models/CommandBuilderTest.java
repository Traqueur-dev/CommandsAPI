package fr.traqueur.commands.api.models;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.requirements.Requirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class CommandBuilderTest {

    private CommandManager<Object, Object> manager;
    private FakePlatform platform;

    @BeforeEach
    void setUp() {
        platform = new FakePlatform();
        manager = new CommandManager<>(platform) {
        };
    }

    // --- Basic building ---

    @Test
    void build_simpleCommand_success() {
        Command<Object, Object> cmd = manager.command("test")
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
        CommandBuilder<Object, Object> builder = manager.command("test")
                .description("Test");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void build_withGameOnly_setsFlag() {
        Command<Object, Object> cmd = manager.command("test")
                .gameOnly()
                .executor((sender, args) -> {
                })
                .build();

        assertTrue(cmd.inGameOnly());
    }

    @Test
    void build_withGameOnlyFalse_clearsFlag() {
        Command<Object, Object> cmd = manager.command("test")
                .gameOnly(false)
                .executor((sender, args) -> {
                })
                .build();

        assertFalse(cmd.inGameOnly());
    }

    // --- Arguments ---

    @Test
    void build_withArgs_addsArguments() {
        Command<Object, Object> cmd = manager.command("test")
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
        Command<Object, Object> cmd = manager.command("test")
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
        Command<Object, Object> cmd = manager.command("test")
                .arg("player", String.class, (sender, args) -> List.of("Alice", "Bob"))
                .executor((sender, args) -> {
                })
                .build();

        assertNotNull(cmd.getArgs().getFirst().tabCompleter());
    }

    // --- Aliases ---

    @Test
    void build_withAlias_addsAlias() {
        Command<Object, Object> cmd = manager.command("test")
                .alias("t")
                .executor((sender, args) -> {
                })
                .build();

        assertEquals(1, cmd.getAliases().size());
        assertTrue(cmd.getAliases().contains("t"));
    }

    @Test
    void build_withAliases_addsMultipleAliases() {
        Command<Object, Object> cmd = manager.command("test")
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
        Command<Object, Object> sub = manager.command("sub")
                .executor((sender, args) -> {
                })
                .build();

        Command<Object, Object> cmd = manager.command("main")
                .subcommand(sub)
                .executor((sender, args) -> {
                })
                .build();

        assertEquals(1, cmd.getSubcommands().size());
        assertSame(sub, cmd.getSubcommands().get(0));
    }

    @Test
    void build_withSubcommands_addsMultipleSubcommands() {
        Command<Object, Object> sub1 = manager.command("sub1")
                .executor((sender, args) -> {
                })
                .build();

        Command<Object, Object> sub2 = manager.command("sub2")
                .executor((sender, args) -> {
                })
                .build();

        Command<Object, Object> cmd = manager.command("main")
                .subcommands(sub1, sub2)
                .executor((sender, args) -> {
                })
                .build();

        assertEquals(2, cmd.getSubcommands().size());
    }

    // --- Requirements ---

    @Test
    void build_withRequirement_addsRequirement() {
        Requirement<Object> req = new Requirement<>() {
            @Override
            public boolean check(Object sender) {
                return true;
            }

            @Override
            public String errorMessage() {
                return "Error";
            }
        };

        Command<Object, Object> cmd = manager.command("test")
                .requirement(req)
                .executor((sender, args) -> {
                })
                .build();

        assertEquals(1, cmd.getRequirements().size());
        assertSame(req, cmd.getRequirements().get(0));
    }

    @Test
    void build_withRequirements_addsMultipleRequirements() {
        Command<Object, Object> cmd = manager.command("test")
                .requirements(
                        new Requirement<>() {
                            @Override
                            public boolean check(Object sender) {
                                return true;
                            }

                            @Override
                            public String errorMessage() {
                                return "";
                            }
                        },
                        new Requirement<>() {
                            @Override
                            public boolean check(Object sender) {
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

        Command<Object, Object> cmd = manager.command("test")
                .executor((sender, args) -> {
                    received.set("executed");
                })
                .build();

        cmd.execute(null, new Arguments(new fr.traqueur.commands.impl.logging.InternalLogger(Logger.getLogger("test"))));

        assertEquals("executed", received.get());
    }

    // --- Register ---

    @Test
    void register_registersCommandInManager() {
        Command<Object, Object> cmd = manager.command("registered")
                .executor((sender, args) -> {
                })
                .register();

        assertTrue(platform.registeredLabels.contains("registered"));
        assertTrue(manager.getCommands().findNode("registered", new String[]{}).isPresent());
    }

    @Test
    void register_returnsBuiltCommand() {
        Command<Object, Object> cmd = manager.command("test")
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
        Command<Object, Object> sub = manager.command("sub")
                .executor((sender, args) -> {
                })
                .build();

        Command<Object, Object> cmd = manager.command("complex")
                .description("Complex command")
                .usage("/complex <arg> [opt]")
                .permission("complex.use")
                .gameOnly()
                .aliases("c", "cx")
                .arg("required", String.class)
                .optionalArg("optional", Integer.class)
                .subcommand(sub)
                .requirement(new Requirement<>() {
                    @Override
                    public boolean check(Object sender) {
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
        Command<Object, Object> cmd = manager.command("minimal")
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
        Command<Object, Object> cmd = manager.command("test")
                .executor((sender, args) -> {
                })
                .build();

        assertTrue(cmd.isEnabled());
    }

    // --- Helper classes ---

    static class FakePlatform implements CommandPlatform<Object, Object> {
        java.util.List<String> registeredLabels = new java.util.ArrayList<>();

        @Override
        public Object getPlugin() {
            return new Object();
        }

        @Override
        public void injectManager(CommandManager<Object, Object> cm) {
        }

        @Override
        public Logger getLogger() {
            return Logger.getAnonymousLogger();
        }

        @Override
        public boolean hasPermission(Object sender, String permission) {
            return true;
        }

        @Override
        public boolean isPlayer(Object sender) {
            return false;
        }

        @Override
        public void sendMessage(Object sender, String message) {
        }

        @Override
        public void addCommand(Command<Object, Object> command, String label) {
            registeredLabels.add(label);
        }

        @Override
        public void removeCommand(String label, boolean sub) {
            registeredLabels.remove(label);
        }
    }
}