package fr.traqueur.testplugin.annotations;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class AnnotedTestCommand {

    @Command(name = "annotedtest", description = "This is a test command", permission = "testplugin.annotedtest",
             usage = "/annotedtest", aliases = {"atc"})
    public void testCommand(CommandSender sender, Integer number, Optional<String> optStr) {
        for(int i = 1; i <= number; i++) {
            sender.sendMessage("This is a test command with number: " + i);
        }
        if(optStr.isPresent()) {
            sender.sendMessage("Optional String: " + optStr.get());
        } else {
            sender.sendMessage("No optional string provided.");
        }
    }

    @SubCommand(parent = "annotedtest", name = "sub", description = "This is a sub command",
                permission = "testplugin.annotedtest.sub", usage = "/annotedtest sub <message>")
    public void subCommand(CommandSender sender, String message) {
        sender.sendMessage("This is a sub command with message: " + message);
    }

    @SubCommand(parent = "annotedtest", name = "sub2", description = "This is a sub command",
            permission = "testplugin.annotedtest.sub", usage = "/annotedtest sub2 <message infinite>")
    public void sub2Command(CommandSender sender, @Arg(name = "message", infinite = true) String message) {
        sender.sendMessage("This is a sub command with message: " + message);
    }

}
