package fr.traqueur.testplugin.annoted;

import fr.traqueur.commands.annotations.*;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandContainer
public class TabCompleteCommands {

    @Command(name = "gamemode", description = "Change your gamemode", permission = "testplugin.gamemode")
    @Alias(value = {"gm"})
    public void gamemode(Player sender, @Arg("mode") String mode) {
        try {
            GameMode gameMode = GameMode.valueOf(mode.toUpperCase());
            sender.setGameMode(gameMode);
            sender.sendMessage("§aGamemode changed to " + gameMode.name());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid gamemode! Use: survival, creative, adventure, or spectator");
        }
    }

    @TabComplete(command = "gamemode", arg = "mode")
    public List<String> completeGamemode(Player sender, String current) {
        return Arrays.stream(GameMode.values())
                .map(gm -> gm.name().toLowerCase())
                .filter(name -> name.startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Command(name = "weather", description = "Change the weather", permission = "testplugin.weather")
    public void weather(Player sender, @Arg("type") String weatherType) {
        switch (weatherType.toLowerCase()) {
            case "clear":
                sender.getWorld().setStorm(false);
                sender.getWorld().setThundering(false);
                sender.sendMessage("§aWeather set to clear!");
                break;
            case "rain":
                sender.getWorld().setStorm(true);
                sender.getWorld().setThundering(false);
                sender.sendMessage("§aWeather set to rain!");
                break;
            case "thunder":
                sender.getWorld().setStorm(true);
                sender.getWorld().setThundering(true);
                sender.sendMessage("§aWeather set to thunder!");
                break;
            default:
                sender.sendMessage("§cInvalid weather type! Use: clear, rain, or thunder");
        }
    }

    @TabComplete(command = "weather", arg = "type")
    public List<String> completeWeather(Player sender, String current) {
        return Arrays.asList("clear", "rain", "thunder").stream()
                .filter(type -> type.startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Command(name = "time", description = "Set the time", permission = "testplugin.time")
    public void time(Player sender, @Arg("preset") String preset) {
        long time;
        switch (preset.toLowerCase()) {
            case "day":
                time = 1000;
                break;
            case "noon":
                time = 6000;
                break;
            case "night":
                time = 13000;
                break;
            case "midnight":
                time = 18000;
                break;
            default:
                sender.sendMessage("§cInvalid time preset! Use: day, noon, night, or midnight");
                return;
        }
        sender.getWorld().setTime(time);
        sender.sendMessage("§aTime set to " + preset + "!");
    }

    @TabComplete(command = "time", arg = "preset")
    public List<String> completeTime() {
        return Arrays.asList("day", "noon", "night", "midnight");
    }
}