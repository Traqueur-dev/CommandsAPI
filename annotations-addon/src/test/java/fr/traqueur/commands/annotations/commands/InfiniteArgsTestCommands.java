package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.*;
import fr.traqueur.commands.test.mocks.*;

import java.util.ArrayList;
import java.util.List;

@CommandContainer
public class InfiniteArgsTestCommands {

    public final List<String> executedCommands = new ArrayList<>();
    public final List<Object[]> executedArgs = new ArrayList<>();

    @Command(name = "broadcast", description = "Broadcast a message")
    public void broadcast(MockSender sender, @Arg("message") @Infinite String message) {
        executedCommands.add("broadcast");
        executedArgs.add(new Object[]{sender, message});
    }

    @Command(name = "kick", description = "Kick a player")
    public void kick(MockSender sender,
                     @Arg("player") String player,
                     @Arg("reason") @Optional @Infinite String reason) {
        executedCommands.add("kick");
        executedArgs.add(new Object[]{sender, player, reason});
    }
}