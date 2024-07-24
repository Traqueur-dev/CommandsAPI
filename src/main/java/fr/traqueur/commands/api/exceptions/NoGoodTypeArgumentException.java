package fr.traqueur.commands.api.exceptions;

/**
 * Exception thrown when an argument has an unexpected type.
 */
public class NoGoodTypeArgumentException extends Exception {

    /**
     * Constructs a new exception with the default message.
     */
    public NoGoodTypeArgumentException() {
        super("Argument has unexpected type.");
    }
}
