package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import fr.traqueur.commands.api.CommandManager;

import java.util.List;

/**
 * The command executor for Velocity.
 * This class implements the RawCommand interface and handles command execution and suggestions.
 *
 * @param <T> The type of the command manager.
 */
public class VelocityExecutor<T> implements RawCommand {

    /**
     * The command manager that this executor uses to manage commands.
     */
    private final CommandManager<T, CommandSource> manager;

    /**
     * Constructs a new Executor with the given command manager.
     *
     * @param manager The command manager to use for this executor.
     */
    public VelocityExecutor(CommandManager<T, CommandSource> manager) {
        this.manager = manager;
    }

    /**
     * Executes the command based on the provided invocation.
     * It checks permissions, requirements, and executes the command if all conditions are met.
     *
     * @param invocation The invocation containing the command source and arguments.
     */
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments().split(" ");
        String label = invocation.alias();
        String labelLower = label.toLowerCase();
        this.manager.getInvoker().invoke(source, labelLower, args);
    }

    /**
     * Suggests completions for the command based on the provided invocation.
     * It checks the command label and returns a list of suggestions based on the current arguments.
     *
     * @param invocation The invocation containing the command source and arguments.
     * @return A list of suggested completions for the command.
     */
    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments().split(" ");
        String label = invocation.alias();
        String labelLower = label.toLowerCase();
        return this.manager.getInvoker().suggest(source, labelLower, args);
    }
}