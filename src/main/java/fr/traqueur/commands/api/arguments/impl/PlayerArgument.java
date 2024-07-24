package fr.traqueur.commands.api.arguments.impl;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Cette classe implémente l'interface ArgumentConverter pour convertir une chaîne de caractères en un objet Player.
 */
public class PlayerArgument implements ArgumentConverter<Player>, TabConverter {

    /**
     * Convertit une chaîne de caractères en un objet Player.
     * @param input La chaîne de caractères représentant le nom du joueur.
     * @return L'objet Player correspondant au nom spécifié, ou null si aucun joueur correspondant n'est trouvé.
     */
    @Override
    public Player apply(String input) {
        return input != null ? Bukkit.getPlayer(input) : null;
    }

    @Override
    public List<String> onCompletion() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}