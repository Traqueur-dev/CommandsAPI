package fr.traqueur.velocityTestPlugin;

import com.velocitypowered.api.command.CommandSource;
import fr.traqueur.commands.api.CommandContext;
import fr.traqueur.commands.velocity.Command;
import net.kyori.adventure.text.Component;

public class TestCommand extends Command<VelocityTestPlugin> {

    public TestCommand(VelocityTestPlugin plugin) {
        super(plugin, "test");
        this.addSubCommand(new SubTestCommand(plugin), new Sub2TestCommand(plugin));
        this.addArgs("test");
        this.addAlias("inner.in");
    }

    @Override
    public void execute(CommandContext<CommandSource> context) {
        int test = context.args().getAsInt("test", -1);
        context.sender().sendMessage(Component.text("Test command executed! " + test));
        context.sender().sendMessage(Component.text(this.getUsage()));
    }
}
