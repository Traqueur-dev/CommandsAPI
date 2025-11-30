package fr.traqueur.commands.api.exceptions;

/**
 * Exception thrown when the updater fails to initialize.
 * This is a runtime exception as initialization failures are typically unrecoverable.
 */
public class UpdaterInitializationException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public UpdaterInitializationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public UpdaterInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public UpdaterInitializationException(Throwable cause) {
        super(cause);
    }
}