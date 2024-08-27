package fr.traqueur.commands.impl.logging;

import fr.traqueur.commands.api.logging.Logger;

public class InternalLogger implements Logger {

    private final java.util.logging.Logger logger;

    public InternalLogger(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void error(String message) {
        this.logger.severe(message);
    }

    @Override
    public void info(String message) {
        this.logger.info(message);
    }
}
