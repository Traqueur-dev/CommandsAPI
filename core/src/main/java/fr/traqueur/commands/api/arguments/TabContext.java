package fr.traqueur.commands.api.arguments;

import java.util.List;

/**
 * Represents the context of a suggestion execution.
 * This class holds the sender of the suggestion and the arguments passed to it.
 *
 * @param <T> The type of the sender (e.g., Player, Console).
 */
public abstract class TabContext<T> {

    /**
     * The sender of the suggestion.
     * This could be a player, console, or any other entity that can send suggestions.
     */
    private final T sender;

    /**
     * The arguments passed to the suggestion.
     * This is a list of strings representing the command arguments.
     */
    private final List<String> args;

    /**
     * Constructs a TabContext with the specified sender and arguments.
     *
     * @param sender The sender of the suggestion.
     * @param args   The arguments passed to the suggestion.
     */
    public TabContext(T sender, List<String> args) {
        this.sender = sender;
        this.args = args;
    }

    /**
     * Gets the sender of the suggestion.
     *
     * @return The sender of the suggestion.
     */
    public T sender() {
        return sender;
    }

    /**
     * Gets the arguments passed to the suggestion.
     *
     * @return The arguments of the suggestion.
     */
    public List<String> args() {
        return args;
    }

}
