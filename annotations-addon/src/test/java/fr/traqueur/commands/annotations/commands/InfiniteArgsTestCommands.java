package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import fr.traqueur.commands.annotations.Infinite;
import fr.traqueur.commands.test.mocks.MockSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                     @Arg("reason") @Infinite Optional<String> reason) {
        executedCommands.add("kick");
        executedArgs.add(new Object[]{sender, player, reason.orElse(null)});
    }
}