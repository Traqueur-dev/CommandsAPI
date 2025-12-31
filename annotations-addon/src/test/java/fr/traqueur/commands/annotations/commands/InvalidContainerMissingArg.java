package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.*;

@CommandContainer
public class InvalidContainerMissingArg {

    @Command(name = "test")
    public void test(MockSender sender, String missingArgAnnotation) {
        // Should fail - second parameter has no @Arg
    }
}