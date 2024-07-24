package fr.traqueur.commands.api.exceptions;

/**
 * Cette exception est levée lorsqu'un argument facultatif est ajouté à une commande avec un argument infini.
 * Cela est invalide car les arguments facultatifs ne peuvent pas suivre les arguments infinis.
 */
public class NoOptionalArgsWithInfiniteArgumentException extends Exception {

    /**
     * Constructeur de la classe NoOptionalArgsWithInfiniteArgumentException.
     */
    public NoOptionalArgsWithInfiniteArgumentException() {
        super("Optional arguments cannot follow infinite arguments.");
    }
}
