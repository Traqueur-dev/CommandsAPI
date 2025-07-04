package fr.traqueur.commands.impl.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.arguments.TabContext;

import java.util.Arrays;
import java.util.List;

/**
 * BooleanArgument is the argument that allow to get a boolean from a string.
 * It's used in the CommandManager to get a boolean from a string.
 * @param <S> the type of the sender
 */
public class BooleanArgument<S> implements ArgumentConverter<Boolean>, TabCompleter<S> {

    /**
     * Creates a new BooleanArgument.
     */
    public BooleanArgument() {
        // Default constructor
    }

    @Override
    public Boolean apply(String s) {
        if(s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(s);
        }
        return null;
    }

    @Override
    public List<String> onCompletion(TabContext<S> context) {
        return Arrays.asList("true", "false");
    }
}
