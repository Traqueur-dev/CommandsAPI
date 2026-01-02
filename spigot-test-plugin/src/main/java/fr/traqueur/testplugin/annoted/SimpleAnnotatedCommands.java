package fr.traqueur.testplugin.annoted;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import org.bukkit.entity.Player;

@CommandContainer
public class SimpleAnnotatedCommands {

    @Command(name = "heal", description = "Heal yourself or another player", permission = "testplugin.heal")
    public void heal(Player sender, @Arg("target") Player target) {
        target.setHealth(20.0);
        target.setFoodLevel(20);
        sender.sendMessage("§a" + target.getName() + " has been healed!");
    }

    @Command(name = "feed", description = "Feed yourself", permission = "testplugin.feed")
    public void feed(Player sender) {
        sender.setFoodLevel(20);
        sender.setSaturation(20.0f);
        sender.sendMessage("§aYou have been fed!");
    }

    @Command(name = "fly", description = "Toggle fly mode", permission = "testplugin.fly")
    public void fly(Player sender) {
        boolean canFly = !sender.getAllowFlight();
        sender.setAllowFlight(canFly);
        sender.setFlying(canFly);
        sender.sendMessage(canFly ? "§aFly mode enabled!" : "§cFly mode disabled!");
    }

    @Command(name = "speed", description = "Set your movement speed", permission = "testplugin.speed")
    public void speed(Player sender, @Arg("speed") int speed) {
        if (speed < 1 || speed > 10) {
            sender.sendMessage("§cSpeed must be between 1 and 10!");
            return;
        }
        float walkSpeed = speed / 10.0f;
        sender.setWalkSpeed(walkSpeed);
        sender.sendMessage("§aWalk speed set to " + speed + "!");
    }
}