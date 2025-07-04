package fr.traqueur.commands.api.arguments;

/**
 * The class Argument.
 * <p> This class is used to represent an argument of a command. </p>
 */
public class Argument<S> {

    /**
     * The argument name.
     * <p>
     *     This is the name of the argument that will be used in the command.
     * </p>
     */
    private final String arg;

    /**
     * The tab completer for this argument.
     * <p>
     *     This is used to provide tab completion for the argument.
     * </p>
     */
    private final TabCompleter<S> tabCompleter;

    /**
     * Constructor for Argument.
     *
     * @param arg The argument name.
     */
    public Argument(String arg, TabCompleter<S> tabCompleter) {
        this.arg = arg;
        this.tabCompleter = tabCompleter;
    }

    /**
     * Get the argument name.
     *
     * @return The argument name.
     */
    public String arg() {
        return this.arg;
    }

    /**
     * Get the tab completer for this argument.
     *
     * @return The tab completer.
     */
    public TabCompleter<S> tabConverter() {
        return this.tabCompleter;
    }

}
