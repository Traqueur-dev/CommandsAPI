package fr.traqueur.commands.spigot;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.CommandContext;
import org.bukkit.command.CommandSender;

/**
 * {@inheritDoc}
 *
 * Represents the context of a command in a Spigot environment.
 * This class extends the CommandContext with CommandSender as the sender type.
 */
public class SpigotCommandContext extends CommandContext<CommandSender> {

    /**
     * Constructs a new SpigotCommandContext with the specified sender and arguments.
     *
     * @param sender The command sender (CommandSender).
     * @param args   The arguments for the command.
     */
    public SpigotCommandContext(CommandSender sender, Arguments args) {
        super(sender, args);
    }
}
