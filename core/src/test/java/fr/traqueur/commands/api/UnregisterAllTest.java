package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.test.mocks.MockCommandManager;
import fr.traqueur.commands.test.mocks.MockSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnregisterAllTest {

    @Test
    void unregisterAll_removesEveryCommandAliasAndSubcommand() {
        MockCommandManager manager = new MockCommandManager();
        Command<Object, MockSender> home = command("home");
        home.addAlias("h");
        home.addSubCommand(command("set"));
        manager.registerCommand(home);
        manager.registerCommand(command("spawn"));

        manager.unregisterAll();

        assertTrue(manager.getMockPlatform().getRegisteredLabels().isEmpty());
        assertTrue(manager.getCommands().getRoot().getChildren().isEmpty());
        assertTrue(manager.getCompleters().isEmpty());
        assertFalse(manager.getInvoker().invoke(null, "home", new String[0]));
    }

    @Test
    void unregisterAll_leavesTheCommandsOfAnotherManager() {
        MockCommandManager first = new MockCommandManager();
        MockCommandManager second = new MockCommandManager();
        first.registerCommand(command("home"));
        Command<Object, MockSender> warp = command("warp");
        warp.addSubCommand(command("list"));
        second.registerCommand(warp);

        first.unregisterAll();

        assertTrue(second.getMockPlatform().hasCommand("warp"));
        assertTrue(second.getMockPlatform().hasCommand("warp.list"));
        assertTrue(second.getCommands().findNode(new String[]{"warp", "list"}).isPresent());
    }

    @Test
    void unregisterAll_skipsCommandsAlreadyUnregistered_andCanBeCalledTwice() {
        MockCommandManager manager = new MockCommandManager();
        Command<Object, MockSender> home = command("home");
        manager.registerCommand(home);
        manager.registerCommand(command("spawn"));
        manager.unregisterCommand(home);

        assertDoesNotThrow(manager::unregisterAll);
        assertDoesNotThrow(manager::unregisterAll);
        assertTrue(manager.getMockPlatform().getRegisteredLabels().isEmpty());
    }

    private static Command<Object, MockSender> command(String name) {
        return new Command<>(null, name) {
            @Override
            public void execute(MockSender sender, Arguments arguments) {
            }
        };
    }
}
