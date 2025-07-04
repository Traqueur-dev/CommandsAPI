package fr.traqueur.testplugin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.CommandContext;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubTestCommand extends Command<TestPlugin> {

    public SubTestCommand(TestPlugin plugin) {
        super(plugin, "sub.inner");
        this.addArgs("test");
        this.addArgs("testStr", String.class, (context) -> {
           context.args().forEach(arg -> {
               context.sender().sendMessage("Arg: " + arg);
           });
           return List.of();
        });
        this.addAlias("sub");
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        int test = context.args().getAsInt("test", -1);
        String testStr = context.args().get("testStr");
        context.sender().sendMessage("Test: " + test + " TestStr: " + testStr);
        context.sender().sendMessage(this.getUsage());
    }
}
