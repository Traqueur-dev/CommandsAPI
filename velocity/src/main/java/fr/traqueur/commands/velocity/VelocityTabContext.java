package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import fr.traqueur.commands.api.arguments.TabContext;

import java.util.List;

/**
 * {@inheritDoc}
 *
 * Represents the context of tab completion in a Spigot environment.
 * This class extends the TabContext with CommandSender as the sender type.
 */
public class VelocityTabContext extends TabContext<CommandSource> {
    public VelocityTabContext(CommandSource sender, List<String> args) {
        super(sender, args);
    }
}
