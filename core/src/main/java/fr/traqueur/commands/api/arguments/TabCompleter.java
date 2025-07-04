package fr.traqueur.commands.api.arguments;

import java.util.List;

/**
 * The class TabConverter.
 * <p>
 *     This class is used to represent a tabulation command converter.
 * </p>
 * @param <S> The type of the sender that will use this tab completer.
 */
@FunctionalInterface
public interface TabCompleter<S> {

    /**
     * This method is called when the tabulation is used.
     * It is used to get the completion of the command.
     * @param context The context of the command.
     * @return The completion of the command.
     */
    List<String> onCompletion(TabContext<S> context);

}
