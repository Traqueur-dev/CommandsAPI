package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import fr.traqueur.commands.test.mocks.*;

import java.util.ArrayList;
import java.util.List;

@CommandContainer
public class SimpleTestCommands {

    public final List<String> executedCommands = new ArrayList<>();
    public final List<Object[]> executedArgs = new ArrayList<>();

    @Command(name = "test", description = "A test command", permission = "test.use")
    public void testCommand(MockSender sender) {
        executedCommands.add("test");
        executedArgs.add(new Object[]{sender});
    }

    @Command(name = "greet", description = "Greet someone")
    public void greetCommand(MockSender sender, @Arg("name") String name) {
        executedCommands.add("greet");
        executedArgs.add(new Object[]{sender, name});
    }

    @Command(name = "add", description = "Add two numbers")
    public void addCommand(MockSender sender, @Arg("a") Integer a, @Arg("b") Integer b) {
        executedCommands.add("add");
        executedArgs.add(new Object[]{sender, a, b});
    }
}