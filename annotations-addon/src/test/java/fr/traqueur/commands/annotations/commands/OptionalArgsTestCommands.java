package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.*;
import fr.traqueur.commands.test.mocks.*;

import java.util.ArrayList;
import java.util.List;

@CommandContainer
public class OptionalArgsTestCommands {

    public final List<String> executedCommands = new ArrayList<>();
    public final List<Object[]> executedArgs = new ArrayList<>();

    @Command(name = "heal", description = "Heal a player")
    public void heal(MockPlayer sender, @Arg("target") @Optional MockPlayer target) {
        executedCommands.add("heal");
        executedArgs.add(new Object[]{sender, target});
    }

    @Command(name = "give", description = "Give items")
    public void give(MockPlayer sender, 
                     @Arg("item") String item,
                     @Arg("amount") @Optional Integer amount) {
        executedCommands.add("give");
        executedArgs.add(new Object[]{sender, item, amount});
    }
}