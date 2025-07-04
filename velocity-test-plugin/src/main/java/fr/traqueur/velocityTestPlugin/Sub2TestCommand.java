package fr.traqueur.velocityTestPlugin;

import com.velocitypowered.api.command.CommandSource;
import fr.traqueur.commands.api.CommandContext;
import fr.traqueur.commands.velocity.Command;
import net.kyori.adventure.text.Component;

public class Sub2TestCommand extends Command<VelocityTestPlugin> {

    public Sub2TestCommand(VelocityTestPlugin plugin) {
        super(plugin, "sub2");
        this.addArgs("test");
        this.addAlias("sub2.inner");
    }


    @Override
    public void execute(CommandContext<CommandSource> context) {
        context.args().getAsInt("test").ifPresent(test -> context.sender().sendMessage(Component.text("Test: " + test)));
        context.sender().sendMessage(Component.text(this.getUsage()));
    }

}
