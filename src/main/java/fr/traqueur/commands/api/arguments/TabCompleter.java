package fr.traqueur.commands.api.arguments;

import java.util.List;

/**
 * The class TabConverter.
 * <p>
 *     This class is used to represent a tabulation command converter.
 * </p>
 */
@FunctionalInterface
public interface TabCompleter<T> {

    /**
     * This method is called when the tabulation is used.
     * It is used to get the completion of the command.
     * @param context The context of the command.
     * @return The completion of the command.
     */
    List<String> onCompletion(TabContext<T> context);

}
