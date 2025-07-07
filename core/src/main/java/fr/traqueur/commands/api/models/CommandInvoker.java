package fr.traqueur.commands.api.models;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.TypeArgumentNotExistException;
import fr.traqueur.commands.api.logging.MessageHandler;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.api.models.collections.CommandTree.MatchResult;
import fr.traqueur.commands.api.requirements.Requirement;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CommandInvoker is responsible for invoking and suggesting commands.
 * It performs lookup, permission and requirement checks, usage display, parsing, and execution.
 *
 * @param <T> plugin type
 * @param <S> sender type
 */
public class CommandInvoker<T, S> {

    private final CommandManager<T, S> manager;

    /**
     * Constructs a CommandInvoker with the given command manager.
     * @param manager the command manager to use for command handling
     */
    public CommandInvoker(CommandManager<T, S> manager) {
        this.manager = manager;
    }

    /**
     * Invokes a command based on the provided source, base label, and raw arguments.
     * @param base the base command label (e.g. "hello")
     * @param rawArgs the arguments of the command
     * @param source the command sender (e.g. a player or console)
     * @return true if a command handler was executed or a message sent; false if command not found
     */
    public boolean invoke(S source, String base, String[] rawArgs) {
        // find matching node
        Optional<MatchResult<T, S>> found = manager.getCommands().findNode(base, rawArgs);
        if (!found.isPresent()) return false;
        MatchResult<T, S> result = found.get();
        CommandTree.CommandNode<T, S> node = result.node;
        Optional<Command<T, S>> cmdOpt = node.getCommand();
        if (!cmdOpt.isPresent()) return false;
        Command<T, S> command = cmdOpt.get();
        String label = node.getFullLabel() != null ? node.getFullLabel() : base;
        String[] args = result.args;

        // in-game check
        if (command.inGameOnly() && !manager.getPlatform().isPlayer(source)) {
            manager.getPlatform().sendMessage(source, manager.getMessageHandler().getOnlyInGameMessage());
            return true;
        }
        // permission check
        String perm = command.getPermission();
        if (!perm.isEmpty() && !manager.getPlatform().hasPermission(source, perm)) {
            manager.getPlatform().sendMessage(source, manager.getMessageHandler().getNoPermissionMessage());
            return true;
        }
        // requirements
        for (Requirement<S> req : command.getRequirements()) {
            if (!req.check(source)) {
                String msg = req.errorMessage().isEmpty()
                        ? manager.getMessageHandler().getRequirementMessage().replace("%requirement%", req.getClass().getSimpleName())
                        : req.errorMessage();
                manager.getPlatform().sendMessage(source, msg);
                return true;
            }
        }
        // usage check
        int min = command.getArgs().size();
        int max = command.isInfiniteArgs() ? Integer.MAX_VALUE : min + command.getOptinalArgs().size();
        if (args.length < min || args.length > max) {
            String usage = command.getUsage().isEmpty()
                    ? command.generateDefaultUsage(manager.getPlatform(), source, label)
                    : command.getUsage();
            manager.getPlatform().sendMessage(source, usage);
            return true;
        }
        // parse and execute
        try {
            Arguments parsed = manager.parse(command, args);
            command.execute(source, parsed);
        } catch (TypeArgumentNotExistException e) {
            manager.getPlatform().sendMessage(source, "&cInternal error: invalid argument type");
            return false;
        } catch (ArgumentIncorrectException e) {
            String msg = manager.getMessageHandler().getArgNotRecognized().replace("%arg%", e.getInput());
            manager.getPlatform().sendMessage(source, msg);
            return true;
        }
        return true;
    }

    /**
     * Suggests command completions based on the provided source, base label, and arguments.
     * This method checks for available tab completers and filters suggestions based on the current input.
     * @param source the command sender (e.g. a player or console)
     * @param base the command label           (e.g. "hello")
     * @param args the arguments provided to the command
     * @return the list of suggestion
     */
    public List<String> suggest(S source, String base, String[] args) {
        for (int i = args.length; i >= 0; i--) {
            String label = buildLabel(base, args, i);
            Map<Integer, TabCompleter<S>> map = manager.getCompleters().get(label);
            if (map != null && map.containsKey(args.length)) {
                return map.get(args.length)
                        .onCompletion(source, Collections.singletonList(buildArgsBefore(base, args)))
                        .stream()
                        .filter(opt -> allowedSuggestion(source, label, opt))
                        .filter(opt -> matchesPrefix(opt, args[args.length - 1]))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private String buildLabel(String base, String[] args, int count) {
        StringBuilder sb = new StringBuilder(base.toLowerCase());
        for (int i = 0; i < count; i++) sb.append('.').append(args[i].toLowerCase());
        return sb.toString();
    }

    private String buildArgsBefore(String base, String[] args) {
        if (args.length <= 1) return base;
        return base + "." + String.join(".", Arrays.copyOf(args, args.length - 1));
    }

    private boolean matchesPrefix(String candidate, String current) {
        String lower = current.toLowerCase();
        return candidate.equalsIgnoreCase(current) || candidate.toLowerCase().startsWith(lower);
    }

    private boolean allowedSuggestion(S src, String label, String opt) {
        String full = label + "." + opt.toLowerCase();
        Optional<Command<T,S>> copt = manager.getCommands().findNode(full.split("\\.")).flatMap(r -> r.node.getCommand());
        if (!copt.isPresent()) return true;
        Command<T,S> c = copt.get();
        return c.getRequirements().stream().allMatch(r -> r.check(src))
                && (c.getPermission().isEmpty() || manager.getPlatform().hasPermission(src, c.getPermission()));
    }
}