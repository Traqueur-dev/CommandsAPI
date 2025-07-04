package fr.traqueur.commands.api.arguments;

/**
 * The class Argument.
 * <p> This class is used to represent an argument of a command. </p>
 */
public class Argument {

    private final String arg;
    private final TabCompleter tabCompleter;

    public Argument(String arg, TabCompleter tabCompleter) {
        this.arg = arg;
        this.tabCompleter = tabCompleter;
    }

    public String arg() {
        return this.arg;
    }

    public TabCompleter tabConverter() {
        return this.tabCompleter;
    }

}
