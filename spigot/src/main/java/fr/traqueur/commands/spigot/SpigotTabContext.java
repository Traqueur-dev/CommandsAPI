package fr.traqueur.commands.spigot;

import fr.traqueur.commands.api.arguments.TabContext;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Represents the context of tab completion in a Spigot environment.
 * This class extends the TabContext with CommandSender as the sender type.
 */
public class SpigotTabContext extends TabContext<CommandSender> {

    /**
     * Constructs a new SpigotTabContext with the specified sender and arguments.
     *
     * @param sender The command sender (CommandSender).
     * @param args   The arguments for the command.
     */
    public SpigotTabContext(CommandSender sender, List<String> args) {
        super(sender, args);
    }
}
