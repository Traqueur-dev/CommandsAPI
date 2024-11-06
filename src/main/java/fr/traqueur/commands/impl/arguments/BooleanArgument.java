package fr.traqueur.commands.impl.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * BooleanArgument is the argument that allow to get a boolean from a string.
 * It's used in the CommandManager to get a boolean from a string.
 */
public class BooleanArgument implements ArgumentConverter<Boolean>, TabCompleter {

    @Override
    public Boolean apply(String s) {
        if(s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(s);
        }
        return null;
    }

    @Override
    public List<String> onCompletion(CommandSender sender, List<String> args) {
        return Arrays.asList("true", "false");
    }
}
