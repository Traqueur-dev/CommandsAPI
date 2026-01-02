package fr.traqueur.testplugin.annoted;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

@CommandContainer
public class OptionalArgsCommands {

    @Command(name = "give", description = "Give items to a player", permission = "testplugin.give")
    public void give(Player sender,
                    @Arg("item") Material material,
                    @Arg("amount") Optional<Integer> amount,
                    @Arg("target") Optional<Player> target) {
        Player recipient = target.orElse(sender);
        int itemAmount = amount.orElse(1);

        if (itemAmount < 1 || itemAmount > 64) {
            sender.sendMessage("§cAmount must be between 1 and 64!");
            return;
        }

        ItemStack item = new ItemStack(material, itemAmount);
        recipient.getInventory().addItem(item);
        sender.sendMessage("§aGave " + itemAmount + " " + material.name() + " to " + recipient.getName());
    }

    @Command(name = "tp", description = "Teleport to a player or location", permission = "testplugin.tp")
    public void teleport(Player sender, @Arg("target") Player target) {
        sender.teleport(target.getLocation());
        sender.sendMessage("§aTeleported to " + target.getName());
    }

    @Command(name = "broadcast", description = "Broadcast a message", permission = "testplugin.broadcast")
    public void broadcast(Player sender,
                         @Arg("message") String message,
                         @Arg("prefix") Optional<String> prefix) {
        String finalMessage = prefix.orElse("§6[BROADCAST]§r") + " " + message;
        Bukkit.broadcastMessage(finalMessage);
        sender.sendMessage("§aMessage broadcasted!");
    }
}