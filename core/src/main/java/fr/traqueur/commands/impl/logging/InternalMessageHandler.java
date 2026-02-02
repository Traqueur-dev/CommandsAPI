package fr.traqueur.commands.impl.logging;

import fr.traqueur.commands.api.logging.MessageHandler;

/**
 * This class is the default implementation of the MessageHandler interface.
 * It provides default messages for the plugin.
 */
public class InternalMessageHandler implements MessageHandler {

    /**
     * Default constructor for the InternalMessageHandler.
     */
    public InternalMessageHandler() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNoPermissionMessage() {
        return "&cYou do not have permission to use this command.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOnlyInGameMessage() {
        return "&cYou can only use this command in-game.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getArgNotRecognized() {
        return "&cArgument &e%arg% &cnot recognized.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequirementMessage() {
        return "The requirement %requirement% was not met";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCommandDisabledMessage() {
        return "&cThis command is currently disabled.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getArgumentTooLongMessage() {
        return "&cArgument &e%arg% &cexceeds maximum length.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInvalidFormatMessage() {
        return "&cInvalid format for argument &e%arg%&c.";
    }

}
