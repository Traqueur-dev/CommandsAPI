package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.CommandContext;

public class VelocityCommandContext extends CommandContext<CommandSource> {

    public VelocityCommandContext(CommandSource sender, Arguments args) {
        super(sender, args);
    }
}
