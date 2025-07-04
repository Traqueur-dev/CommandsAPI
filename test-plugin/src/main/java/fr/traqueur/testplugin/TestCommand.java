package fr.traqueur.testplugin;

import fr.traqueur.commands.api.CommandContext;
import fr.traqueur.commands.spigot.Command;
import org.bukkit.command.CommandSender;

public class TestCommand extends Command<TestPlugin> {

    public TestCommand(TestPlugin plugin) {
        super(plugin, "test");
        this.addSubCommand(new SubTestCommand(plugin), new Sub2TestCommand(plugin));
        this.addArgs("test");
        this.addAlias("inner.in");
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        int test = context.args().getAsInt("test", -1);
        context.sender().sendMessage("Test command executed! " + test);
        context.sender().sendMessage(this.getUsage());
    }
}
