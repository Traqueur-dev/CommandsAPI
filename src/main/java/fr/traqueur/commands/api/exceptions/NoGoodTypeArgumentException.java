package fr.traqueur.commands.api.exceptions;

/**
 * Cette exception est levée lorsqu'un argument n'a pas le type attendu.
 */
public class NoGoodTypeArgumentException extends Exception {

    /**
     * Constructeur de la classe NoGoodTypeArgumentException.
     */
    public NoGoodTypeArgumentException() {
        super("Argument has unexpected type.");
    }
}
