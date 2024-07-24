package fr.traqueur.commands.api.arguments.impl;


import fr.traqueur.commands.api.arguments.ArgumentConverter;

/**
 * Cette classe implémente l'interface ArgumentConverter pour convertir une chaîne de caractères en un objet Integer.
 */
public class IntegerArgument implements ArgumentConverter<Integer> {

    /**
     * Convertit une chaîne de caractères en un objet Integer.
     * @param input La chaîne de caractères représentant le nombre à convertir.
     * @return L'objet Integer correspondant à la chaîne de caractères spécifiée, ou null si la conversion échoue.
     */
    @Override
    public Integer apply(String input) {
        try {
            return Integer.valueOf(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
