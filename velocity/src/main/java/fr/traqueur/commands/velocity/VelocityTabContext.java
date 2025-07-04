package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import fr.traqueur.commands.api.arguments.TabContext;

import java.util.List;

/**
 * Represents the context of tab completion in a Spigot environment.
 * This class extends the TabContext with CommandSender as the sender type.
 */
public class VelocityTabContext extends TabContext<CommandSource> {

    /**
     * Constructs a new VelocityTabContext with the specified sender and arguments.
     *
     * @param sender The command sender (CommandSource).
     * @param args   The arguments for the command.
     */
    public VelocityTabContext(CommandSource sender, List<String> args) {
        super(sender, args);
    }
}
