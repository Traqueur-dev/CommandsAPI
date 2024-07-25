package fr.traqueur.commands.api.lang.impl;

import fr.traqueur.commands.api.lang.MessageHandler;

public class InternalMessageHandler implements MessageHandler {

    @Override
    public String getNoPermissionMessage() {
        return "&cYou do not have permission to use this command.";
    }

    @Override
    public String getOnlyInGameMessage() {
        return "&cYou can only use this command in-game.";
    }

    @Override
    public String getMissingArgsMessage() {
        return "&cMissing arguments.";
    }

    @Override
    public String getArgNotRecognized() {
        return "&cArgument &e%arg% &cnot recognized.";
    }
}
