package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.Infinite;
import fr.traqueur.commands.api.description.ArgumentDescription;
import fr.traqueur.commands.api.description.CommandDescription;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.test.mocks.MockCommandManager;
import fr.traqueur.commands.test.mocks.MockSender;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DescribeTest {

    @Test
    void describe_givesTheTreeOfTheRegisteredCommands() {
        MockCommandManager manager = new MockCommandManager();
        Command<Object, MockSender> home = command("home");
        home.addAlias("h");
        home.setDescription("Teleport home");
        home.setPermission("valkn.home");
        home.addOptionalArgs("name", String.class);
        Command<Object, MockSender> set = command("set");
        set.setGameOnly(true);
        set.addArgs("name", String.class);
        set.addOptionalArgs("note", Infinite.class);
        home.addSubCommand(set);
        manager.registerCommand(home);
        Command<Object, MockSender> spawn = command("spawn");
        spawn.setUsage("/spawn, simply");
        manager.registerCommand(spawn);

        List<CommandDescription> described = manager.describe();

        assertEquals(List.of("home", "spawn"), described.stream().map(CommandDescription::name).toList());
        CommandDescription homeDescription = described.get(0);
        assertEquals(List.of("h"), homeDescription.aliases());
        assertEquals("Teleport home", homeDescription.description());
        assertEquals("valkn.home", homeDescription.permission());
        assertEquals("/home <set>|[name:string]", homeDescription.usage());
        assertEquals(List.of(new ArgumentDescription("name", String.class, true, false)), homeDescription.optionalArguments());
        assertTrue(homeDescription.requiredArguments().isEmpty());

        CommandDescription setDescription = homeDescription.subcommands().get(0);
        assertEquals("home set", setDescription.path());
        assertTrue(setDescription.inGameOnly());
        assertEquals(List.of("name"), setDescription.requiredArguments().stream().map(ArgumentDescription::name).toList());
        ArgumentDescription note = setDescription.optionalArguments().get(0);
        assertTrue(note.infinite());
        assertEquals("infinite", note.typeName());
        assertEquals("/home set <name:string> [note:infinite]", setDescription.usage());

        assertEquals("/spawn, simply", described.get(1).usage());
    }

    @Test
    void describe_followsUnregistration_andIsReadOnly() {
        MockCommandManager manager = new MockCommandManager();
        Command<Object, MockSender> home = command("home");
        manager.registerCommand(home);
        manager.registerCommand(command("spawn"));
        manager.unregisterCommand(home);

        List<CommandDescription> described = manager.describe();

        assertEquals(List.of("spawn"), described.stream().map(CommandDescription::name).toList());
        assertThrows(UnsupportedOperationException.class, () -> described.get(0).arguments().add(null));
    }

    private static Command<Object, MockSender> command(String name) {
        return new Command<>(null, name) {
            @Override
            public void execute(MockSender sender, Arguments arguments) {
            }
        };
    }
}
