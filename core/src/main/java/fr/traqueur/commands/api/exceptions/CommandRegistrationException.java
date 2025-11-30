package fr.traqueur.commands.api.exceptions;

/**
 * Exception thrown when a command registration fails.
 * This is a runtime exception as registration failures are typically unrecoverable.
 */
public class CommandRegistrationException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public CommandRegistrationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public CommandRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public CommandRegistrationException(Throwable cause) {
        super(cause);
    }
}