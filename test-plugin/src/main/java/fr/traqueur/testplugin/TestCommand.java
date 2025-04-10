package fr.traqueur.testplugin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class TestCommand extends Command<TestPlugin> {

    public TestCommand(TestPlugin plugin) {
        super(plugin, "test");
        this.addSubCommand(new SubTestCommand(plugin), new Sub2TestCommand(plugin));
        this.addArgs("test");
        this.addAlias("inner.in");
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        int test = args.getAsInt("test", -1);
        sender.sendMessage("Test command executed! " + test);
        sender.sendMessage(this.getUsage());
    }
}
