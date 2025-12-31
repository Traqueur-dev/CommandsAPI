package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.*;

import java.util.ArrayList;
import java.util.List;

@CommandContainer
public class AliasTestCommands {

    public final List<String> executedCommands = new ArrayList<>();

    @Command(name = "gamemode", description = "Change game mode")
    @Alias({"gm"})
    public void gamemode(MockPlayer sender, @Arg("mode") String mode) {
        executedCommands.add("gamemode:" + mode);
    }

    @Command(name = "teleport", description = "Teleport to player")
    @Alias({"tp", "tpto", "goto"})
    public void teleport(MockPlayer sender, @Arg("target") String target) {
        executedCommands.add("teleport:" + target);
    }

    @Command(name = "spawn")
    @Alias({"hub", "lobby", "s"})
    public void spawn(MockPlayer sender) {
        executedCommands.add("spawn");
    }
}