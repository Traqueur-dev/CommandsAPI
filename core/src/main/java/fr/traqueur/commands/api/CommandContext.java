package fr.traqueur.commands.api;

public abstract class CommandContext<T> {

    private final T sender;
    private final Arguments args;

    public CommandContext(T sender, Arguments args) {
        this.sender = sender;
        this.args = args;
    }

    public T sender() {
        return sender;
    }

    public Arguments args() {
        return args;
    }

}
