package fr.traqueur.testplugin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import org.bukkit.command.CommandSender;

public class TestCommand extends Command<TestPlugin> {

    public TestCommand(TestPlugin plugin) {
        super(plugin, "test");
        this.addSubCommand(new SubTestCommand(plugin));
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        sender.sendMessage("Test command executed!");
    }
}
