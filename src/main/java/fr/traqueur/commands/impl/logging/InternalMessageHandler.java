package fr.traqueur.commands.impl.logging;

import fr.traqueur.commands.api.logging.MessageHandler;

/**
 * This class is the default implementation of the MessageHandler interface.
 * It provides default messages for the plugin.
 */
public class InternalMessageHandler implements MessageHandler {

    /**
     * Get the message to send when the player does not have permission to use a command.
     *
     * @return the message
     */
    @Override
    public String getNoPermissionMessage() {
        return "&cYou do not have permission to use this command.";
    }

    /**
     * Get the message to send when the sender is not a player.
     *
     * @return the message
     */
    @Override
    public String getOnlyInGameMessage() {
        return "&cYou can only use this command in-game.";
    }

    /**
     * Get the message to send when an argument is missing.
     *
     * @return the message
     */
    @Override
    public String getMissingArgsMessage() {
        return "&cMissing arguments.";
    }

    /**
     * Get the message to send when an argument is not recognized.
     *
     * @return the message
     */
    @Override
    public String getArgNotRecognized() {
        return "&cArgument &e%arg% &cnot recognized.";
    }

    @Override
    public String getRequirementMessage() {
        return "The requirement %requirement% was not met";
    }

    @Override
    public String getToManyArgsMessage() {
        return "&cToo many arguments.";
    }
}
