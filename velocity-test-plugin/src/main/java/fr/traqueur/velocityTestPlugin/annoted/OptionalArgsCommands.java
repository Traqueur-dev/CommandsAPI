package fr.traqueur.velocityTestPlugin.annoted;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

@CommandContainer
public class OptionalArgsCommands {

    @Command(name = "vkick", description = "Kick a player from the proxy", permission = "testplugin.kick")
    public void kick(CommandSource sender,
                    @Arg("player") Player target,
                    @Arg("reason") Optional<String> reason) {
        String kickReason = reason.orElse("Kicked from the proxy");
        target.disconnect(Component.text(kickReason, NamedTextColor.RED));
        sender.sendMessage(Component.text(target.getUsername() + " has been kicked!", NamedTextColor.GREEN));
    }

    @Command(name = "vannounce", description = "Send an announcement", permission = "testplugin.announce")
    public void announce(CommandSource sender,
                        @Arg("message") String message,
                        @Arg("color") Optional<String> color) {
        NamedTextColor textColor;
        try {
            textColor = color.map(c -> NamedTextColor.NAMES.value(c.toLowerCase()))
                    .orElse(NamedTextColor.YELLOW);
        } catch (Exception e) {
            textColor = NamedTextColor.YELLOW;
        }

        Component announcement = Component.text("[ANNOUNCEMENT] ", NamedTextColor.GOLD)
                .append(Component.text(message, textColor));

        // This would broadcast to all players on the proxy
        sender.sendMessage(Component.text("Announcement sent!", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("(Broadcast requires proxy instance)", NamedTextColor.GRAY));
    }

    @Command(name = "vfind", description = "Find which server a player is on", permission = "testplugin.find")
    public void find(CommandSource sender, @Arg("player") Player target) {
        target.getCurrentServer().ifPresentOrElse(
            server -> sender.sendMessage(Component.text(target.getUsername() + " is on: " +
                server.getServerInfo().getName(), NamedTextColor.GREEN)),
            () -> sender.sendMessage(Component.text(target.getUsername() + " is not connected to any server",
                NamedTextColor.YELLOW))
        );
    }
}