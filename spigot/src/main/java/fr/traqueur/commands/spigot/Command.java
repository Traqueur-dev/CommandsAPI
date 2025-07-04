package fr.traqueur.commands.spigot;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Command<T extends JavaPlugin> extends fr.traqueur.commands.api.Command<T, CommandSender> {

    public Command(T plugin, String name) {
        super(plugin, name);
    }



}
