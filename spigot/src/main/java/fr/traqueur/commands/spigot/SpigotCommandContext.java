package fr.traqueur.commands.spigot;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.CommandContext;
import org.bukkit.command.CommandSender;

public class SpigotCommandContext extends CommandContext<CommandSender> {
    public SpigotCommandContext(CommandSender sender, Arguments args) {
        super(sender, args);
    }
}
