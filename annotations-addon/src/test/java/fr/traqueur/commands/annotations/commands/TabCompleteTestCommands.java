package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CommandContainer
public class TabCompleteTestCommands {

    public final List<String> executedCommands = new ArrayList<>();
    public final List<String> tabCompleteInvocations = new ArrayList<>();

    private final List<String> availableWorlds = Arrays.asList("world", "world_nether", "world_the_end");
    private final List<String> availableWarps = Arrays.asList("spawn", "shop", "arena", "hub");

    @Command(name = "world", description = "Change world")
    public void world(MockPlayer sender, @Arg("world") String world) {
        executedCommands.add("world:" + world);
    }

    @TabComplete(command = "world", arg = "world")
    public List<String> completeWorld(MockPlayer sender, String current) {
        tabCompleteInvocations.add("world:" + current);
        return availableWorlds.stream()
            .filter(w -> w.toLowerCase().startsWith(current.toLowerCase()))
            .toList();
    }

    @Command(name = "warp", description = "Warp to location")
    public void warp(MockPlayer sender, @Arg("name") String name) {
        executedCommands.add("warp:" + name);
    }

    @TabComplete(command = "warp", arg = "name")
    public List<String> completeWarp(MockPlayer sender, String current) {
        tabCompleteInvocations.add("warp:" + current);
        return availableWarps.stream()
            .filter(w -> w.toLowerCase().startsWith(current.toLowerCase()))
            .toList();
    }

    // Tab completer without parameters
    @Command(name = "gamemode", description = "Change gamemode")
    public void gamemode(MockPlayer sender, @Arg("mode") String mode) {
        executedCommands.add("gamemode:" + mode);
    }

    @TabComplete(command = "gamemode", arg = "mode")
    public List<String> completeGamemode() {
        tabCompleteInvocations.add("gamemode:no-args");
        return Arrays.asList("survival", "creative", "adventure", "spectator");
    }
}