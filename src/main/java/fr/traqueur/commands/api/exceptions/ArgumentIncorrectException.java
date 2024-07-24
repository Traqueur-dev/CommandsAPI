package fr.traqueur.commands.api.exceptions;

/**
 * Cette exception est levée lorsqu'un argument est incorrect.
 */
public class ArgumentIncorrectException extends Exception {

    /**
     * La chaîne de caractères représentant l'argument incorrect.
     */
    private final String input;

    /**
     * Constructeur de la classe ArgumentIncorrectException.
     * @param input La chaîne de caractères représentant l'argument incorrect.
     */
    public ArgumentIncorrectException(String input) {
        super("Argument incorrect: " + input);
        this.input = input;
    }

    /**
     * Renvoie la chaîne de caractères représentant l'argument incorrect.
     * @return La chaîne de caractères représentant l'argument incorrect.
     */
    public String getInput() {
        return input;
    }
}
