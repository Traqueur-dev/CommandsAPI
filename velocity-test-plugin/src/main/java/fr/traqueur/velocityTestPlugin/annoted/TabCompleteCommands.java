package fr.traqueur.velocityTestPlugin.annoted;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.traqueur.commands.annotations.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandContainer
public class TabCompleteCommands {

    @Command(name = "vserver", description = "Connect to a server", permission = "testplugin.server")
    @Alias(value = {"vconnect", "vgo"})
    public void server(Player player, @Arg("server") String serverName) {
        // This would connect the player to the specified server
        player.sendMessage(Component.text("Connecting to " + serverName + "...", NamedTextColor.GREEN));
        player.sendMessage(Component.text("(Server connection requires proxy instance)", NamedTextColor.GRAY));
    }

    @TabComplete(command = "vserver", arg = "server")
    public List<String> completeServer(CommandSource sender, String current) {
        // In a real scenario, this would get servers from the proxy
        List<String> servers = Arrays.asList("lobby", "survival", "creative", "minigames", "skyblock");
        return servers.stream()
                .filter(server -> server.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Command(name = "vsend", description = "Send a player to a server", permission = "testplugin.send")
    public void send(CommandSource sender,
                    @Arg("player") Player target,
                    @Arg("server") String serverName) {
        sender.sendMessage(Component.text("Sending " + target.getUsername() + " to " + serverName,
            NamedTextColor.GREEN));
        sender.sendMessage(Component.text("(Server send requires proxy instance)", NamedTextColor.GRAY));
    }

    @TabComplete(command = "vsend", arg = "server")
    public List<String> completeSendServer(CommandSource sender, String current) {
        List<String> servers = Arrays.asList("lobby", "survival", "creative", "minigames", "skyblock");
        return servers.stream()
                .filter(server -> server.toLowerCase().startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Command(name = "valert", description = "Send an alert with a type", permission = "testplugin.alert")
    public void alert(CommandSource sender,
                     @Arg("type") String alertType,
                     @Arg("message") String message) {
        NamedTextColor color;
        switch (alertType.toLowerCase()) {
            case "info":
                color = NamedTextColor.AQUA;
                break;
            case "warning":
                color = NamedTextColor.YELLOW;
                break;
            case "error":
                color = NamedTextColor.RED;
                break;
            case "success":
                color = NamedTextColor.GREEN;
                break;
            default:
                sender.sendMessage(Component.text("Invalid alert type!", NamedTextColor.RED));
                return;
        }

        sender.sendMessage(Component.text("[" + alertType.toUpperCase() + "] ", color)
                .append(Component.text(message, NamedTextColor.WHITE)));
    }

    @TabComplete(command = "valert", arg = "type")
    public List<String> completeAlertType() {
        return Arrays.asList("info", "warning", "error", "success");
    }
}