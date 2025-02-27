package fr.traqueur.testplugin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Sub2TestCommand extends Command<TestPlugin> {

    public Sub2TestCommand(TestPlugin plugin) {
        super(plugin, "sub2");
        this.addArgs("test");
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        args.getAsInt("test").ifPresent(test -> sender.sendMessage("Test: " + test));
        sender.sendMessage(this.getUsage());
    }
}
