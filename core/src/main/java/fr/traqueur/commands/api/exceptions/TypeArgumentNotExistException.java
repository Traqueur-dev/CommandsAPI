package fr.traqueur.commands.api.exceptions;

/**
 * Exception thrown when an type of an argument is not found.
 */
public class TypeArgumentNotExistException extends RuntimeException {

    /**
     * Constructs a new exception with the default message.
     */
    public TypeArgumentNotExistException() {
        super("Required argument not found");
    }
}