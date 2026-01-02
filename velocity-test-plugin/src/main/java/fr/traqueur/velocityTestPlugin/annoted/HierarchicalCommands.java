package fr.traqueur.velocityTestPlugin.annoted;

import com.velocitypowered.api.command.CommandSource;
import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

@CommandContainer
public class HierarchicalCommands {

    @Command(name = "vproxy", description = "Proxy management commands", permission = "testplugin.proxy")
    public void proxy(CommandSource sender) {
        sender.sendMessage(Component.text("=== Proxy Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/vproxy info - Show proxy information", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/vproxy servers - List all servers", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/vproxy players - List all players", NamedTextColor.YELLOW));
    }

    @Command(name = "vproxy.info", description = "Show proxy information", permission = "testplugin.proxy.info")
    public void proxyInfo(CommandSource sender) {
        sender.sendMessage(Component.text("=== Proxy Info ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Velocity Test Plugin", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("(Detailed info requires proxy instance)", NamedTextColor.GRAY));
    }

    @Command(name = "vproxy.servers", description = "List all servers", permission = "testplugin.proxy.servers")
    public void proxyServers(CommandSource sender) {
        sender.sendMessage(Component.text("=== Registered Servers ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("(Server list requires proxy instance)", NamedTextColor.GRAY));
    }

    @Command(name = "vproxy.players", description = "List all players", permission = "testplugin.proxy.players")
    public void proxyPlayers(CommandSource sender) {
        sender.sendMessage(Component.text("=== Online Players ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("(Player list requires proxy instance)", NamedTextColor.GRAY));
    }

    @Command(name = "vadmin", description = "Admin commands", permission = "testplugin.admin")
    public void admin(CommandSource sender) {
        sender.sendMessage(Component.text("=== Admin Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/vadmin maintenance <on|off> - Toggle maintenance mode", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/vadmin whitelist <player> - Add player to whitelist", NamedTextColor.YELLOW));
    }

    @Command(name = "vadmin.maintenance", description = "Toggle maintenance mode", permission = "testplugin.admin.maintenance")
    public void adminMaintenance(CommandSource sender, @Arg("mode") String mode) {
        boolean enabled = mode.equalsIgnoreCase("on");
        sender.sendMessage(Component.text("Maintenance mode " + (enabled ? "enabled" : "disabled"),
            enabled ? NamedTextColor.RED : NamedTextColor.GREEN));
    }

    @Command(name = "vadmin.whitelist", description = "Add player to whitelist", permission = "testplugin.admin.whitelist")
    public void adminWhitelist(CommandSource sender, @Arg("player") String playerName) {
        sender.sendMessage(Component.text(playerName + " added to whitelist!", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("(Whitelist feature not fully implemented)", NamedTextColor.GRAY));
    }

    @Command(name = "vconfig", description = "Configuration commands", permission = "testplugin.config")
    public void config(CommandSource sender) {
        sender.sendMessage(Component.text("=== Config Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/vconfig reload - Reload configuration", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/vconfig reload.all - Reload all configs", NamedTextColor.YELLOW));
    }

    @Command(name = "vconfig.reload", description = "Reload configuration", permission = "testplugin.config.reload")
    public void configReload(CommandSource sender) {
        sender.sendMessage(Component.text("Configuration reloaded!", NamedTextColor.GREEN));
    }

    @Command(name = "vconfig.reload.all", description = "Reload all configurations", permission = "testplugin.config.reload")
    public void configReloadAll(CommandSource sender) {
        sender.sendMessage(Component.text("All configurations reloaded!", NamedTextColor.GREEN));
    }
}