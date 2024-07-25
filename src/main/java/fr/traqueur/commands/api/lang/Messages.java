package fr.traqueur.commands.api.lang;

public enum Messages {

    NO_PERMISSION,
    ONLY_IN_GAME,
    MISSING_ARGS,
    ARG_NOT_RECOGNIZED,
    ;

    public String getKey() {
        return this.name().toLowerCase();
    }

}
