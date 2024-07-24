package fr.traqueur.commands.api.arguments.impl;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabConverter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cette classe implémente l'interface ArgumentConverter pour convertir une chaîne de caractères en un objet OfflinePlayer.
 */
public class OfflinePlayerArgument implements ArgumentConverter<OfflinePlayer>, TabConverter {

    /**
     * Convertit une chaîne de caractères en un objet OfflinePlayer.
     * @param input La chaîne de caractères représentant le nom du joueur.
     * @return L'objet OfflinePlayer correspondant au nom spécifié, ou null si aucun joueur correspondant n'est trouvé.
     */
    @Override
    public OfflinePlayer apply(String input) {
        return input != null ? Bukkit.getOfflinePlayer(input) : null;
    }

    @Override
    public List<String> onCompletion() {
        return Arrays.stream(Bukkit.getServer().getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList());
    }
}
