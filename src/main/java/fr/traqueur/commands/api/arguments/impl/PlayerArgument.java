package fr.traqueur.commands.api.arguments.impl;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabConverter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Argument converter for {@link Player}.
 */
public class PlayerArgument implements ArgumentConverter<Player>, TabConverter {

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses {@link Bukkit#getPlayer(String)} to convert the input to a player.
     */
    @Override
    public Player apply(String input) {
        return input != null ? Bukkit.getPlayer(input) : null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns a list of all online player names.
     */
    @Override
    public List<String> onCompletion(CommandSender sender) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}