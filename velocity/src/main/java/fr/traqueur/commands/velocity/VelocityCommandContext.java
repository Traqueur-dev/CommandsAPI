package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.CommandContext;

/**
 * Represents the context of a command in a Spigot environment.
 * This class extends the CommandContext with CommandSender as the sender type.
 */
public class VelocityCommandContext extends CommandContext<CommandSource> {

    /**
     * Constructs a new VelocityCommandContext with the specified sender and arguments.
     *
     * @param sender The command sender (CommandSource).
     * @param args   The arguments for the command.
     */
    public VelocityCommandContext(CommandSource sender, Arguments args) {
        super(sender, args);
    }
}
