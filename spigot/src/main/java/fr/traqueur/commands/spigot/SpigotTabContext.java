package fr.traqueur.commands.spigot;

import fr.traqueur.commands.api.arguments.TabContext;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * {@inheritDoc}
 *
 * Represents the context of tab completion in a Spigot environment.
 * This class extends the TabContext with CommandSender as the sender type.
 */
public class SpigotTabContext extends TabContext<CommandSender> {
    public SpigotTabContext(CommandSender sender, List<String> args) {
        super(sender, args);
    }
}
