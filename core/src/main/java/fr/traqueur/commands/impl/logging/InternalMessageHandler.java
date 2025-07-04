package fr.traqueur.commands.impl.logging;

import fr.traqueur.commands.api.logging.MessageHandler;

/**
 * This class is the default implementation of the MessageHandler interface.
 * It provides default messages for the plugin.
 */
public class InternalMessageHandler implements MessageHandler {

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

}
