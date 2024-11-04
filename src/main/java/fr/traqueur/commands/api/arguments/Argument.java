package fr.traqueur.commands.api.arguments;

/**
 * The class Argument.
 * <p> This class is used to represent an argument of a command. </p>
 */
public class Argument {

    private final String arg;
    private final TabConverter tabConverter;

    public Argument(String arg, TabConverter tabConverter) {
        this.arg = arg;
        this.tabConverter = tabConverter;
    }

    public String arg() {
        return this.arg;
    }

    public TabConverter tabConverter() {
        return this.tabConverter;
    }

}
