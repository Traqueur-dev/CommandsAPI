package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.*;
import fr.traqueur.commands.test.mocks.*;

import java.util.ArrayList;
import java.util.List;

@CommandContainer
public class OrphanTestCommands {

    public final List<String> executedCommands = new ArrayList<>();

    // No "warp" parent defined - core will handle it
    @Command(name = "warp.set", description = "Set a warp", permission = "warp.set")
    public void warpSet(MockPlayer sender, @Arg("name") String name) {
        executedCommands.add("warp.set:" + name);
    }

    @Command(name = "warp.delete", description = "Delete a warp")
    public void warpDelete(MockPlayer sender, @Arg("name") String name) {
        executedCommands.add("warp.delete:" + name);
    }

    // Deep orphan - no "config" or "config.database" defined
    @Command(name = "config.database.reset", description = "Reset database")
    public void configDatabaseReset(MockSender sender) {
        executedCommands.add("config.database.reset");
    }
}