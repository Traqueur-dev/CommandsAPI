package fr.traqueur.commands.paper;

import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.models.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class PaperPlatformTest {

    private final List<String> warnings = new ArrayList<>();
    private PaperPlatform<JavaPlugin> platform;
    private CommandManager<JavaPlugin> manager;
    private Commands registrar;

    @BeforeEach
    void setUp() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                warnings.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        when(plugin.getLogger()).thenReturn(logger);
        when(plugin.getName()).thenReturn("Valkn");
        platform = new PaperPlatform<>(plugin, false);
        manager = new CommandManager<>(platform);
        registrar = mock(Commands.class);
        when(registrar.register(any(LiteralCommandNode.class), any(), anyCollection()))
                .thenAnswer(invocation -> Set.of(((LiteralCommandNode<?>) invocation.getArgument(0)).getLiteral()));
    }

    @Test
    void commandsEvent_registersEveryRootLabelWithItsDescription() {
        Command<JavaPlugin, CommandSender> home = command("home");
        home.setDescription("Go home");
        home.addAlias("h");
        manager.registerCommand(home);
        manager.registerCommand(command("spawn"));

        platform.registerAll(registrar);

        ArgumentCaptor<LiteralCommandNode<CommandSourceStack>> nodes = ArgumentCaptor.forClass(LiteralCommandNode.class);
        ArgumentCaptor<String> descriptions = ArgumentCaptor.forClass(String.class);
        verify(registrar, times(3)).register(nodes.capture(), descriptions.capture(), anyCollection());
        List<String> labels = nodes.getAllValues().stream().map(LiteralCommandNode::getLiteral).sorted().toList();
        assertEquals(List.of("h", "home", "spawn"), labels);
        int homeIndex = nodes.getAllValues().stream().map(LiteralCommandNode::getLiteral).toList().indexOf("home");
        assertEquals("Go home", descriptions.getAllValues().get(homeIndex));
        assertTrue(warnings.isEmpty());
    }

    @Test
    void labelRegisteredAfterTheEvent_isReported() {
        manager.registerCommand(command("home"));
        platform.registerAll(registrar);

        manager.registerCommand(command("late"));

        assertTrue(warnings.stream().anyMatch(w -> w.contains("'late'") && w.contains("/minecraft:reload")));
    }

    @Test
    void labelHeldByAnotherCommand_isReported() {
        manager.registerCommand(command("give"));
        when(registrar.register(any(LiteralCommandNode.class), any(), anyCollection())).thenReturn(Set.of("valkn:give"));

        platform.registerAll(registrar);

        assertTrue(warnings.stream().anyMatch(w -> w.contains("'/valkn:give' answers instead")));
    }

    private static Command<JavaPlugin, CommandSender> command(String name) {
        return new Command<>(null, name) {
            @Override
            public void execute(CommandSender sender, Arguments arguments) {
            }
        };
    }
}
