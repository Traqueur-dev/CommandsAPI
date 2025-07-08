package fr.traqueur.velocityTestPlugin;

import com.velocitypowered.api.command.CommandSource;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.velocity.Command;
import net.kyori.adventure.text.Component;

import java.util.List;

public class SubTestCommand extends Command<VelocityTestPlugin> {

    public SubTestCommand(VelocityTestPlugin plugin) {
        super(plugin, "sub.inner");
        this.addArgs("test");
        this.addArgs("testStr", String.class, (sender, args) -> {
           args.forEach(arg -> {
               sender.sendMessage(Component.text("Arg: " + arg));
           });
           return List.of();
        });
        this.addAlias("sub");
    }

    @Override
    public void execute(CommandSource sender, Arguments args) {
        int test = args.getAsInt("test", -1);
        String testStr = args.get("testStr");
        sender.sendMessage(Component.text("Test: " + test + " TestStr: " + testStr));
        sender.sendMessage(Component.text(this.getUsage()));
    }
}
