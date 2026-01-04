package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import fr.traqueur.commands.test.mocks.MockSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Test commands using primitive types (int, boolean, double, long) instead of wrappers.
 * This tests that the ArgumentConverter.Wrapper properly normalizes primitive types to wrappers.
 */
@CommandContainer
public class PrimitiveTypesTestCommands {

    public final List<String> executedCommands = new ArrayList<>();
    public final List<Object[]> executedArgs = new ArrayList<>();

    @Command(name = "primitiveint", description = "Test command with primitive int")
    public void primitiveIntCommand(MockSender sender, @Arg("value") int value) {
        executedCommands.add("primitiveint");
        executedArgs.add(new Object[]{sender, value});
    }

    @Command(name = "primitivelong", description = "Test command with primitive long")
    public void primitiveLongCommand(MockSender sender, @Arg("value") long value) {
        executedCommands.add("primitivelong");
        executedArgs.add(new Object[]{sender, value});
    }

    @Command(name = "primitivedouble", description = "Test command with primitive double")
    public void primitiveDoubleCommand(MockSender sender, @Arg("value") double value) {
        executedCommands.add("primitivedouble");
        executedArgs.add(new Object[]{sender, value});
    }

    @Command(name = "primitivebool", description = "Test command with primitive boolean")
    public void primitiveBooleanCommand(MockSender sender, @Arg("enabled") boolean enabled) {
        executedCommands.add("primitivebool");
        executedArgs.add(new Object[]{sender, enabled});
    }

    @Command(name = "mixedprimitives", description = "Test command with multiple primitive types")
    public void mixedPrimitivesCommand(MockSender sender,
                                       @Arg("count") int count,
                                       @Arg("enabled") boolean enabled,
                                       @Arg("ratio") double ratio) {
        executedCommands.add("mixedprimitives");
        executedArgs.add(new Object[]{sender, count, enabled, ratio});
    }
}