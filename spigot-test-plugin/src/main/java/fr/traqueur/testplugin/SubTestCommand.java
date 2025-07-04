package fr.traqueur.testplugin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubTestCommand extends Command<TestPlugin> {

    public SubTestCommand(TestPlugin plugin) {
        super(plugin, "sub.inner");
        this.addArgs("test");
        this.addArgs("testStr", String.class, (sender, args) -> {
           args.forEach(arg -> {
               sender.sendMessage("Arg: " + arg);
           });
           return List.of();
        });
        this.addAlias("sub");
    }

    @Override
    public void execute(CommandSender sender, Arguments arguments) {
        int test = arguments.getAsInt("test", -1);
        String testStr = arguments.get("testStr");
        sender.sendMessage("Test: " + test + " TestStr: " + testStr);
        sender.sendMessage(this.getUsage());
    }
}
