package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.MockSender;

// Missing @CommandContainer - should throw error
public class InvalidContainerNoAnnotation {

    @Command(name = "test")
    public void test(MockSender sender) {
    }
}