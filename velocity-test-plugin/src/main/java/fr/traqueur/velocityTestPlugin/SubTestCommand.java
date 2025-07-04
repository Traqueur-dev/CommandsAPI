package fr.traqueur.velocityTestPlugin;

import com.velocitypowered.api.command.CommandSource;
import fr.traqueur.commands.api.CommandContext;
import fr.traqueur.commands.velocity.Command;
import net.kyori.adventure.text.Component;

import java.util.List;

public class SubTestCommand extends Command<VelocityTestPlugin> {

    public SubTestCommand(VelocityTestPlugin plugin) {
        super(plugin, "sub.inner");
        this.addArgs("test");
        this.addArgs("testStr", String.class, (context) -> {
           context.args().forEach(arg -> {
               context.sender().sendMessage(Component.text("Arg: " + arg));
           });
           return List.of();
        });
        this.addAlias("sub");
    }

    @Override
    public void execute(CommandContext<CommandSource> context) {
        int test = context.args().getAsInt("test", -1);
        String testStr = context.args().get("testStr");
        context.sender().sendMessage(Component.text("Test: " + test + " TestStr: " + testStr));
        context.sender().sendMessage(Component.text(this.getUsage()));
    }
}
