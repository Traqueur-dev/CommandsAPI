package fr.traqueur.commands.api.exceptions;

/**
 * Cette exception est levée lorsqu'un argument est ajouté à une commande avec un argument infini.
 * Cela est invalide car les arguments ne peuvent pas suivre les arguments infinis.
 */
public class NoArgsWithInfiniteArgumentException extends Exception {

    /**
     * Constructeur de la classe NoOptionalArgsWithInfiniteArgumentException.
     */
    public NoArgsWithInfiniteArgumentException(boolean optional) {
        super((optional ? "Optional arguments" : "Arguments") + "cannot follow infinite arguments.");
    }
}
