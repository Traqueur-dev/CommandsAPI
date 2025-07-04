package fr.traqueur.commands.api.arguments;

import java.util.List;

public abstract class TabContext<T> {

    private final T sender;
    private final List<String> args;

    public TabContext(T sender, List<String> args) {
        this.sender = sender;
        this.args = args;
    }

    public T sender() {
        return sender;
    }

    public List<String> args() {
        return args;
    }

}
