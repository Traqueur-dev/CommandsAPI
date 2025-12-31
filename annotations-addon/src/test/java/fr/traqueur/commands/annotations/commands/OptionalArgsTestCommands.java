package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import fr.traqueur.commands.test.mocks.MockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CommandContainer
public class OptionalArgsTestCommands {

    public final List<String> executedCommands = new ArrayList<>();
    public final List<Object[]> executedArgs = new ArrayList<>();

    @Command(name = "heal", description = "Heal a player")
    public void heal(MockPlayer sender, @Arg("target") Optional<MockPlayer> target) {
        executedCommands.add("heal");
        executedArgs.add(new Object[]{sender, target.orElse(null)});
    }

    @Command(name = "give", description = "Give items")
    public void give(MockPlayer sender,
                     @Arg("item") String item,
                     @Arg("amount") Optional<Integer> amount) {
        executedCommands.add("give");
        executedArgs.add(new Object[]{sender, item, amount.orElse(null)});
    }
}