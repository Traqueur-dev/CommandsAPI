package fr.traqueur.commands.api.exceptions;

/**
 * This exception is thrown when arguments are present after an infinite argument.
 */
public class ArgsWithInfiniteArgumentException extends Exception {

    /**
     * Create a new instance of the exception with the default message.
     * @param optional if the argument is optional
     */
    public ArgsWithInfiniteArgumentException(boolean optional) {
        super((optional ? "Optional arguments" : "Arguments") + " cannot follow infinite arguments.");
    }
}
