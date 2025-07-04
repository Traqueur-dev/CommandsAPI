package fr.traqueur.commands.api;

/**
 * Represents the context of a command execution.
 * This class holds the sender of the command and the arguments passed to it.
 *
 * @param <T> The type of the sender (e.g., Player, Console).
 */
public abstract class CommandContext<T> {

    /**
     * The sender of the command.
     * This could be a player, console, or any other entity that can send commands.
     */
    private final T sender;

    /**
     * The arguments passed to the command.
     * This is an instance of Arguments which contains the parsed command arguments.
     */
    private final Arguments args;

    /**
     * Constructs a CommandContext with the specified sender and arguments.
     *
     * @param sender The sender of the command.
     * @param args   The arguments passed to the command.
     */
    public CommandContext(T sender, Arguments args) {
        this.sender = sender;
        this.args = args;
    }

    /**
     * Gets the sender of the command.
     *
     * @return The sender of the command.
     */
    public T sender() {
        return sender;
    }

    /**
     * Gets the arguments passed to the command.
     *
     * @return The arguments of the command.
     */
    public Arguments args() {
        return args;
    }

}
