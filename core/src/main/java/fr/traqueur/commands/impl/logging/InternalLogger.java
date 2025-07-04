package fr.traqueur.commands.impl.logging;

import fr.traqueur.commands.api.logging.Logger;

/**
 * An internal logger that uses java.util.logging to log messages.
 * This logger is used internally by the command framework.
 */
public class InternalLogger implements Logger {

    /**
     * The java.util.logging.Logger instance used for logging.
     */
    private final java.util.logging.Logger logger;

    /**
     * Constructor that initializes the logger with a java.util.logging.Logger instance.
     *
     * @param logger The java.util.logging.Logger instance to use for logging.
     */
    public InternalLogger(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String message) {
        this.logger.severe(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String message) {
        this.logger.info(message);
    }
}
