package fr.traqueur.commands.api.arguments.impl;


import fr.traqueur.commands.api.arguments.ArgumentConverter;

/**
 * Cette classe implémente l'interface ArgumentConverter pour convertir une chaîne de caractères en un objet Long.
 */
public class LongArgument implements ArgumentConverter<Long> {

    /**
     * Convertit une chaîne de caractères en un objet Long.
     * @param input La chaîne de caractères représentant le nombre à convertir.
     * @return L'objet Long correspondant à la chaîne de caractères spécifiée.
     */
    @Override
    public Long apply(String input) {
        try {
            return Long.valueOf(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
