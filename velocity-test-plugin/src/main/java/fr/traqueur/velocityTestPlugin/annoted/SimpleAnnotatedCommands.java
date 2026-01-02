package fr.traqueur.velocityTestPlugin.annoted;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@CommandContainer
public class SimpleAnnotatedCommands {

    @Command(name = "vping", description = "Check proxy latency", permission = "testplugin.ping")
    public void ping(Player player) {
        long ping = player.getPing();
        player.sendMessage(Component.text("Your ping: " + ping + "ms", NamedTextColor.GREEN));
    }

    @Command(name = "vlist", description = "List online players", permission = "testplugin.list")
    public void list(CommandSource sender) {
        sender.sendMessage(Component.text("Online players:", NamedTextColor.GOLD));
        // This would list players from the proxy
        sender.sendMessage(Component.text("Feature requires proxy instance", NamedTextColor.YELLOW));
    }

    @Command(name = "vinfo", description = "Get player info", permission = "testplugin.info")
    public void info(CommandSource sender, @Arg("player") Player target) {
        sender.sendMessage(Component.text("=== Player Info ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Name: " + target.getUsername(), NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("UUID: " + target.getUniqueId(), NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Ping: " + target.getPing() + "ms", NamedTextColor.YELLOW));
        target.getCurrentServer().ifPresent(server ->
            sender.sendMessage(Component.text("Server: " + server.getServerInfo().getName(), NamedTextColor.YELLOW))
        );
    }

    @Command(name = "vmessage", description = "Send a message to a player", permission = "testplugin.message")
    public void message(Player sender, @Arg("target") Player target, @Arg("message") String message) {
        target.sendMessage(Component.text("[" + sender.getUsername() + " -> You] ", NamedTextColor.GRAY)
                .append(Component.text(message, NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("[You -> " + target.getUsername() + "] ", NamedTextColor.GRAY)
                .append(Component.text(message, NamedTextColor.WHITE)));
    }
}