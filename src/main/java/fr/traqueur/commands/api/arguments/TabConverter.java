package fr.traqueur.commands.api.arguments;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The class TabConverter.
 * <p>
 *     This class is used to represent a tabulation command converter.
 * </p>
 */
@FunctionalInterface
public interface TabConverter {

    /**
     * This method is called when the tabulation is used.
     * It is used to get the completion of the command.
     * @param sender The sender of the command.
     * @return The completion of the command.
     */
    List<String> onCompletion(CommandSender sender, List<String> args);

}
