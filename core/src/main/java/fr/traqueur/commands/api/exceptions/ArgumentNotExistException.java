package fr.traqueur.commands.api.exceptions;

/**
 * This exception is thrown when the argument does not exist.
 */
public class ArgumentNotExistException extends RuntimeException {

    /**
     * Create a new instance of the exception with the default message.
     */
    public ArgumentNotExistException() {
        super("Argument does not exist.");
    }
}
