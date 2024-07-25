package fr.traqueur.commands.api.lang;

public interface MessageHandler {

    String getNoPermissionMessage();

    String getOnlyInGameMessage();

    String getMissingArgsMessage();

    String getArgNotRecognized();

    default String getMessage(Messages type) {
        return switch (type) {
            case NO_PERMISSION -> getNoPermissionMessage();
            case ONLY_IN_GAME -> getOnlyInGameMessage();
            case MISSING_ARGS -> getMissingArgsMessage();
            case ARG_NOT_RECOGNIZED -> getArgNotRecognized();
        };
    }
}
