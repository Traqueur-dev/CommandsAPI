package fr.traqueur.commands.api.arguments;

import java.util.Objects;

/**
 * The class Argument.
 * <p> This class is used to represent an argument of a command. </p>
 *
 * @param <S>          The type of the sender that will use this argument.
 * @param name         The argument name.
 *                     <p>
 *                     This is the name of the argument that will be used in the command.
 *                     </p>
 * @param tabCompleter The tab completer for this argument.
 *                     <p>
 *                     This is used to provide tab completion for the argument.
 *                     </p>
 */
public record Argument<S>(String name, ArgumentType type, TabCompleter<S> tabCompleter) {

    /**
     * Constructor for Argument.
     *
     * @param name         The argument name.
     * @param type         The argument type.
     * @param tabCompleter The tab completer for this argument.
     */
    public Argument(String name, ArgumentType type, TabCompleter<S> tabCompleter) {
        this.name = Objects.requireNonNull(name, "Argument name cannot be null");
        this.type = Objects.requireNonNull(type, "Argument type cannot be null");
        this.tabCompleter = tabCompleter;
    }


    /**
     * Create an argument without tab completer.
     *
     * @param name the argument name
     * @param type the argument type
     */
    public Argument(String name, ArgumentType type) {
        this(name, type, null);
    }

    public String canonicalName() {
        return this.name + ":" + this.type.key().getSimpleName().toLowerCase();
    }


    /**
     * Check if this argument is infinite.
     *
     * @return true if infinite type
     */
    public boolean isInfinite() {
        return type.isInfinite();
    }

}
