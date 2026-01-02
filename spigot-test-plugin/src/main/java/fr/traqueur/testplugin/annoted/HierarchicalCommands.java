package fr.traqueur.testplugin.annoted;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

@CommandContainer
public class HierarchicalCommands {

    @Command(name = "admin", description = "Admin management commands", permission = "testplugin.admin")
    public void admin(Player sender) {
        sender.sendMessage("§6=== Admin Commands ===");
        sender.sendMessage("§e/admin kick <player> [reason] - Kick a player");
        sender.sendMessage("§e/admin ban <player> [reason] - Ban a player");
        sender.sendMessage("§e/admin mute <player> - Mute a player");
    }

    @Command(name = "admin.kick", description = "Kick a player", permission = "testplugin.admin.kick")
    public void adminKick(Player sender,
                         @Arg("player") Player target,
                         @Arg("reason") Optional<String> reason) {
        String kickReason = reason.orElse("Kicked by an administrator");
        target.kickPlayer("§c" + kickReason);
        sender.sendMessage("§a" + target.getName() + " has been kicked!");
        Bukkit.broadcastMessage("§e" + target.getName() + " was kicked by " + sender.getName());
    }

    @Command(name = "admin.ban", description = "Ban a player", permission = "testplugin.admin.ban")
    public void adminBan(Player sender,
                        @Arg("player") Player target,
                        @Arg("reason") Optional<String> reason) {
        String banReason = reason.orElse("Banned by an administrator");
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(target.getName(), banReason, null, sender.getName());
        target.kickPlayer("§cYou have been banned!\n§7Reason: " + banReason);
        sender.sendMessage("§a" + target.getName() + " has been banned!");
        Bukkit.broadcastMessage("§e" + target.getName() + " was banned by " + sender.getName());
    }

    @Command(name = "admin.mute", description = "Mute a player", permission = "testplugin.admin.mute")
    public void adminMute(Player sender, @Arg("player") Player target) {
        sender.sendMessage("§a" + target.getName() + " has been muted! (Feature not fully implemented)");
    }

    @Command(name = "config", description = "Configuration commands", permission = "testplugin.config")
    public void config(Player sender) {
        sender.sendMessage("§6=== Config Commands ===");
        sender.sendMessage("§e/config reload - Reload configuration");
        sender.sendMessage("§e/config reload all - Reload all configs");
        sender.sendMessage("§e/config reload messages - Reload messages");
    }

    @Command(name = "config.reload", description = "Reload configuration", permission = "testplugin.config.reload")
    public void configReload(Player sender) {
        sender.sendMessage("§aConfiguration reloaded!");
    }

    @Command(name = "config.reload.all", description = "Reload all configurations", permission = "testplugin.config.reload")
    public void configReloadAll(Player sender) {
        sender.sendMessage("§aAll configurations reloaded!");
    }

    @Command(name = "config.reload.messages", description = "Reload message configuration", permission = "testplugin.config.reload")
    public void configReloadMessages(Player sender) {
        sender.sendMessage("§aMessage configuration reloaded!");
    }
}