package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import fr.traqueur.commands.annotations.Default;
import fr.traqueur.commands.test.mocks.MockSender;

@CommandContainer
public class InvalidDefaultTestCommands {

    @Command(name = "broken")
    public void broken(MockSender sender, @Arg("amount") @Default("lots") int amount) {
    }
}
