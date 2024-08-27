package fr.traqueur.commands.impl.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabConverter;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * BooleanArgument is the argument that allow to get a boolean from a string.
 * It's used in the CommandManager to get a boolean from a string.
 */
public class BooleanArgument implements ArgumentConverter<Boolean>, TabConverter {

    @Override
    public Boolean apply(String s) {
        if(s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(s);
        }
        return null;
    }

    @Override
    public List<String> onCompletion(CommandSender sender) {
        return List.of("true", "false");
    }
}