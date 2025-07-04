package fr.traqueur.commands.impl.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.arguments.TabContext;

import java.util.Arrays;
import java.util.List;

/**
 * BooleanArgument is the argument that allow to get a boolean from a string.
 * It's used in the CommandManager to get a boolean from a string.
 */
public class BooleanArgument<T> implements ArgumentConverter<Boolean>, TabCompleter<T> {

    @Override
    public Boolean apply(String s) {
        if(s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(s);
        }
        return null;
    }

    @Override
    public List<String> onCompletion(TabContext<T> context) {
        return Arrays.asList("true", "false");
    }
}
