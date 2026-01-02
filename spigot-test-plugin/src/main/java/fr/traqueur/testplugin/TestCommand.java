package fr.traqueur.testplugin;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;

public class TestCommand extends Command<TestPlugin> {

    public TestCommand(TestPlugin plugin) {
        super(plugin, "test");
        this.addSubCommand(new SubTestCommand(plugin), new Sub2TestCommand(plugin));
        this.addArgs("test", Integer.class);
        this.addAlias("inner.in");
    }

    @Override
    public void execute(CommandSender sender, Arguments arguments) {
        int test = arguments.get("test");
        sender.sendMessage("Test command executed! " + test);
        sender.sendMessage(this.getUsage());
    }
}
