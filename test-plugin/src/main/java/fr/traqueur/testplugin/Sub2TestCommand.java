package fr.traqueur.testplugin;

import fr.traqueur.commands.api.CommandContext;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;


public class Sub2TestCommand extends Command<TestPlugin> {

    public Sub2TestCommand(TestPlugin plugin) {
        super(plugin, "sub2");
        this.addArgs("test");
        this.addAlias("sub2.inner");
    }


    @Override
    public void execute(CommandContext<CommandSender> context) {
        context.args().getAsInt("test").ifPresent(test -> context.sender().sendMessage("Test: " + test));
        context.sender().sendMessage(this.getUsage());
    }

}
