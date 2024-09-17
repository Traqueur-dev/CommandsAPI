package fr.traqueur.testplugin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;

public class SubTestCommand extends Command<TestPlugin> {

    public SubTestCommand(TestPlugin plugin) {
        super(plugin, "sub");
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        sender.sendMessage("SubTest command executed!");
    }
}
