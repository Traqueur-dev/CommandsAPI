package fr.traqueur.commands.api.arguments;

import java.util.List;

/**
 * The class TabConverter.
 * <p>
 *     This class is used to represent a tabulation command converter.
 * </p>
 */
public interface TabConverter {

    List<String> onCompletion();

}
