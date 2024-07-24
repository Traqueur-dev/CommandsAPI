package fr.traqueur.commands.api.exceptions;

/**
 * Exception thrown when an argument is incorrect.
 */
public class ArgumentIncorrectException extends Exception {

    /**
     * The input that caused the exception.
     */
    private final String input;

    /**
     * Constructor.
     *
     * @param input The input that caused the exception.
     */
    public ArgumentIncorrectException(String input) {
        super("Argument incorrect: " + input);
        this.input = input;
    }

    /**
     * Get the input that caused the exception.
     * @return The input.
     */
    public String getInput() {
        return input;
    }
}
