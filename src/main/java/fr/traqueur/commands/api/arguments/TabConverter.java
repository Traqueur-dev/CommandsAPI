package fr.traqueur.commands.api.arguments;

import java.util.List;

/**
 * The class TabConverter.
 * <p>
 *     This class is used to represent a tabulation command converter.
 * </p>
 */
public interface TabConverter {

    /**
     * This method is called when the tabulation is used.
     * It is used to get the completion of the command.
     * @return The completion of the command.
     */
    List<String> onCompletion();

}
