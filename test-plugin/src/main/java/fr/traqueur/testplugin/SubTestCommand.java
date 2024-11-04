package fr.traqueur.testplugin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubTestCommand extends Command<TestPlugin> {

    public SubTestCommand(TestPlugin plugin) {
        super(plugin, "sub");
        this.addArgs("test", Integer.class);
        this.addArgs("testStr", String.class, (sender, args) -> {
           args.forEach(arg -> {
               sender.sendMessage("Arg: " + arg);
           });
           return List.of();
        });
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        int test = args.get("test");
        String testStr = args.get("testStr");
        sender.sendMessage("Test: " + test + " TestStr: " + testStr);
    }
}
