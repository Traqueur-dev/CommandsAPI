package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.TypeArgumentNotExistException;
import fr.traqueur.commands.api.requirements.Requirement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CommandInvoker is responsible for invoking and suggesting commands.
 * It performs lookup, permission and requirement checks, usage display, parsing, and execution.
 *
 * @param <T> plugin type
 * @param <S> sender type
 */
public class CommandInvoker<T, S> {

    private static class CommandEntry {
        String label;
        String[] args;

        CommandEntry(String label, String[] args) {
            this.label = label;
            this.args = args;
        }
    }

    /**
     * The CommandManager instance that manages commands and their configurations.
     */
    private final CommandManager<T, S> manager;

    /**
     * Constructs a CommandInvoker with the specified CommandManager.
     *
     * @param manager the CommandManager instance to use
     */
    public CommandInvoker(CommandManager<T, S> manager) {
        this.manager = manager;
    }

    /**
     * Invokes a command based on the provided source, base label, and raw arguments.
     * It checks for command existence, permissions, requirements, usage, and executes the command if valid.
     *
     * @param source the sender of the command
     * @param base the base label of the command
     * @param rawArgs the raw arguments passed to the command
     * @return true if the command was successfully invoked, false otherwise
     */
    public boolean invoke(S source, String base, String[] rawArgs) {
        CommandEntry entry = findCommand(base, rawArgs);
        if (entry == null) return false;
        String label = entry.label;
        Command<T, S> command = manager.getCommands().get(label);
        String[] modArgs = entry.args;

        if (checkInGame(source, command) || checkPermission(source, command) || checkRequirements(source, command)) {
            return true;
        }

        if (checkUsage(source, command, label, modArgs)) {
            return true;
        }

        return parseAndExecute(source, command, modArgs);
    }

    /**
     * Suggests command completions based on the provided source, base label, and arguments.
     * It checks for matching commands and applies filters based on permissions and requirements.
     *
     * @param source the sender of the command
     * @param base the base label of the command
     * @param args the arguments passed to the command
     * @return a list of suggested completions
     */
    public List<String> suggest(S source, String base, String[] args) {
        for (int i = args.length; i >= 0; i--) {
            String label = buildLabel(base, args, i);
            Map<Integer, TabCompleter<S>> map = manager.getCompleters().get(label);
            if (map != null && map.containsKey(args.length)) {
                return map.get(args.length).onCompletion(source, Collections.singletonList(buildArgsBefore(base, args)))
                        .stream()
                        .filter(opt -> allowedSuggestion(source, label, opt))
                        .filter(s -> matchesPrefix(s, args[args.length - 1]))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private CommandEntry findCommand(String base, String[] args) {
        for (int i = args.length; i >= 0; i--) {
            String label = buildLabel(base, args, i);
            if (manager.getCommands().containsKey(label)) {
                String[] mod = Arrays.copyOfRange(args, i, args.length);
                return new CommandEntry(label, mod);
            }
        }
        return null;
    }

    private boolean checkInGame(S src, Command<T,S> cmd) {
        if (cmd.inGameOnly() && !manager.getPlatform().isPlayer(src)) {
            manager.getPlatform().sendMessage(src, manager.getMessageHandler().getOnlyInGameMessage());
            return true;
        }
        return false;
    }

    private boolean checkPermission(S src, Command<T,S> cmd) {
        String perm = cmd.getPermission();
        if (!perm.isEmpty() && !manager.getPlatform().hasPermission(src, perm)) {
            manager.getPlatform().sendMessage(src, manager.getMessageHandler().getNoPermissionMessage());
            return true;
        }
        return false;
    }

    private boolean checkRequirements(S src, Command<T,S> cmd) {
        for (Requirement<S> r : cmd.getRequirements()) {
            if (!r.check(src)) {
                String msg = r.errorMessage().isEmpty()
                        ? manager.getMessageHandler().getRequirementMessage().replace("%requirement%", r.getClass().getSimpleName())
                        : r.errorMessage();
                manager.getPlatform().sendMessage(src, msg);
                return true;
            }
        }
        return false;
    }

    private boolean checkUsage(S src, Command<T,S> cmd, String label, String[] modArgs) {
        int min = cmd.getArgs().size();
        int max = cmd.isInfiniteArgs() ? Integer.MAX_VALUE : min + cmd.getOptinalArgs().size();
        if (modArgs.length < min || modArgs.length > max) {
            String usage = cmd.getUsage().isEmpty()
                    ? cmd.generateDefaultUsage(manager.getPlatform(), src, label)
                    : cmd.getUsage();
            manager.getPlatform().sendMessage(src, usage);
            return true;
        }
        return false;
    }

    private boolean parseAndExecute(S src, Command<T,S> cmd, String[] modArgs) {
        try {
            Arguments args = manager.parse(cmd, modArgs);
            cmd.execute(src, args);
        } catch (TypeArgumentNotExistException e) {
            manager.getPlatform().sendMessage(src, "&cInternal error: invalid argument type");
            return false;
        } catch (ArgumentIncorrectException e) {
            String msg = manager.getMessageHandler().getArgNotRecognized().replace("%arg%", e.getInput());
            manager.getPlatform().sendMessage(src, msg);
        }
        return true;
    }

    private String buildLabel(String base, String[] args, int count) {
        StringBuilder sb = new StringBuilder(base.toLowerCase());
        for (int i = 0; i < count; i++) sb.append('.').append(args[i].toLowerCase());
        return sb.toString();
    }

    private String buildArgsBefore(String base, String[] args) {
        return base + "." + String.join(".", Arrays.copyOf(args, args.length - 1));
    }

    private boolean matchesPrefix(String candidate, String current) {
        String lower = current.toLowerCase();
        return candidate.equalsIgnoreCase(current) || candidate.toLowerCase().startsWith(lower);
    }

    private boolean allowedSuggestion(S src, String label, String opt) {
        String full = label + "." + opt.toLowerCase();
        Command<T,S> c = manager.getCommands().get(full);
        if (c == null) return true;
        return c.getRequirements().stream().allMatch(r -> r.check(src))
                && (c.getPermission().isEmpty() || manager.getPlatform().hasPermission(src, c.getPermission()));
    }
}
