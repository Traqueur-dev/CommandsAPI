package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import fr.traqueur.commands.annotations.Default;
import fr.traqueur.commands.annotations.Infinite;
import fr.traqueur.commands.test.mocks.MockSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CommandContainer
public class DefaultArgsTestCommands {

    public final List<Object> received = new ArrayList<>();

    @Command(name = "give")
    public void give(MockSender sender, @Arg("item") String item, @Arg("amount") @Default("1") int amount) {
        received.add(item + "x" + amount);
    }

    @Command(name = "heal")
    public void heal(MockSender sender, @Arg("amount") @Default("20") Optional<Double> amount) {
        received.add(amount);
    }

    @Command(name = "say")
    public void say(MockSender sender, @Arg("text") @Infinite @Default("hello there") String text) {
        received.add(text);
    }
}
