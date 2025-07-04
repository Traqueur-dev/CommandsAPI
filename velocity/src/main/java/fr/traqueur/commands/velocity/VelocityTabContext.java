package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import fr.traqueur.commands.api.arguments.TabContext;

import java.util.List;

public class VelocityTabContext extends TabContext<CommandSource> {
    public VelocityTabContext(CommandSource sender, List<String> args) {
        super(sender, args);
    }
}
