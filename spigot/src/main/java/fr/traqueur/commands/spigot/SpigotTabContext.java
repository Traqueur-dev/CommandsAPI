package fr.traqueur.commands.spigot;

import fr.traqueur.commands.api.arguments.TabContext;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SpigotTabContext extends TabContext<CommandSender> {
    public SpigotTabContext(CommandSender sender, List<String> args) {
        super(sender, args);
    }
}
