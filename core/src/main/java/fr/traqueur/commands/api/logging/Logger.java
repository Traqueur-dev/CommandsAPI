package fr.traqueur.commands.api.logging;

/**
 * Represents a logger.
 */
public interface Logger {

    /**
     * Logs an error message.
     * @param message The message to log.
     */
    void error(String message);

    /**
     * Logs an information message.
     * @param message The message to log.
     */
    void info(String message);

}
