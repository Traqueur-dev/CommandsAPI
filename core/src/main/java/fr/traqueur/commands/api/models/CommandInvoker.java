package fr.traqueur.commands.api.models;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.TypeArgumentNotExistException;
import fr.traqueur.commands.api.logging.MessageKey;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.api.models.collections.CommandTree.MatchResult;
import fr.traqueur.commands.api.requirements.Requirement;
import fr.traqueur.commands.api.utils.Patterns;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CommandInvoker is responsible for invoking and suggesting commands.
 * It performs lookup, permission and requirement checks, usage display, parsing, and execution.
 *
 * @param manager the command manager to use for command handling
 * @param <T>     plugin type
 * @param <S>     sender type
 */
public record CommandInvoker<T, S>(CommandManager<T, S> manager) {

    /**
     * Invokes a command based on the provided source, base label, and raw arguments.
     *
     * @param base    the base command label (e.g. "hello")
     * @param rawArgs the arguments of the command
     * @param source  the command sender (e.g. a player or console)
     * @return true if a command handler was executed or a message sent; false if command not found
     */
    public boolean invoke(S source, String base, String[] rawArgs) {
        Optional<CommandContext<T, S>> contextOpt = findCommandContext(base, rawArgs);
        if (contextOpt.isEmpty()) {
            return false;
        }

        CommandContext<T, S> context = contextOpt.get();

        if (!validateCommandExecution(source, context)) {
            return true;
        }

        return executeCommand(source, context);
    }

    /**
     * Find and prepare command context.
     *
     * @param base    the base command label
     * @param rawArgs the raw arguments
     * @return the command context if found
     */
    private Optional<CommandContext<T, S>> findCommandContext(String base, String[] rawArgs) {
        Optional<MatchResult<T, S>> found = manager.getCommands().findNode(base, rawArgs);
        if (found.isEmpty()) {
            return Optional.empty();
        }

        MatchResult<T, S> result = found.get();
        CommandTree.CommandNode<T, S> node = result.node();
        Optional<Command<T, S>> cmdOpt = node.getCommand();

        if (cmdOpt.isEmpty()) {
            return Optional.empty();
        }

        Command<T, S> command = cmdOpt.get();
        String label = node.getFullLabel() != null ? node.getFullLabel() : base;
        String[] args = result.args();

        return Optional.of(new CommandContext<>(command, label, args));
    }

    /**
     * Validate command execution conditions (enabled, in-game, permissions, requirements, usage).
     *
     * @param source  the command sender
     * @param context the command context
     * @return true if all validations passed, false otherwise (message already sent to user)
     */
    private boolean validateCommandExecution(S source, CommandContext<T, S> context) {
        return checkEnabled(source, context.command)
                && checkInGameOnly(source, context.command)
                && checkPermission(source, context.command)
                && checkRequirements(source, context.command)
                && checkUsage(source, context);
    }

    /**
     * Check if command is enabled.
     *
     * @param source  the command sender
     * @param command the command to check
     * @return true if command is enabled
     */
    private boolean checkEnabled(S source, Command<T, S> command) {
        if (!command.isEnabled()) {
            this.sendMessage(source, MessageKey.DISABLED, manager.getMessageHandler().getCommandDisabledMessage(), Map.of());
            return false;
        }
        return true;
    }

    /**
     * Check if command requires in-game execution.
     *
     * @param source  the command sender
     * @param command the command to check
     * @return true if check passed or not applicable
     */
    private boolean checkInGameOnly(S source, Command<T, S> command) {
        if (command.inGameOnly() && !manager.getPlatform().isPlayer(source)) {
            this.sendMessage(source, MessageKey.ONLY_IN_GAME, manager.getMessageHandler().getOnlyInGameMessage(), Map.of());
            return false;
        }
        return true;
    }

    /**
     * Check if sender has required permission.
     *
     * @param source  the command sender
     * @param command the command to check
     * @return true if check passed or no permission required
     */
    private boolean checkPermission(S source, Command<T, S> command) {
        String perm = command.getPermission();
        if (!perm.isEmpty() && !manager.getPlatform().hasPermission(source, perm)) {
            this.sendMessage(source, MessageKey.NO_PERMISSION, manager.getMessageHandler().getNoPermissionMessage(), Map.of());
            return false;
        }
        return true;
    }

    /**
     * Check if all requirements are satisfied.
     *
     * @param source  the command sender
     * @param command the command to check
     * @return true if all requirements passed
     */
    private boolean checkRequirements(S source, Command<T, S> command) {
        for (Requirement<S> req : command.getRequirements()) {
            if (!req.check(source)) {
                String msg = buildRequirementMessage(req);
                String reason = req.errorMessage().isEmpty() ? requirementName(req) : req.errorMessage();
                this.sendMessage(source, MessageKey.REQUIREMENT, msg, Map.of(MessageKey.REQUIREMENT_VALUE, reason));
                return false;
            }
        }
        return true;
    }

    /**
     * Build error message for failed requirement.
     *
     * @param req the failed requirement
     * @return the error message
     */
    private String buildRequirementMessage(Requirement<S> req) {
        return req.errorMessage().isEmpty()
                ? manager.getMessageHandler().getRequirementMessage()
                .replace("%requirement%", requirementName(req))
                : req.errorMessage();
    }

    /**
     * Name a requirement for the generic message, falling back on its class name when
     * {@link Requirement#name()} gives nothing usable.
     *
     * @param req the requirement
     * @return the name to show
     */
    private String requirementName(Requirement<S> req) {
        String name = req.name();
        return name == null || name.isBlank() ? req.getClass().getSimpleName() : name;
    }

    /**
     * Check if argument count is valid.
     *
     * @param source  the command sender
     * @param context the command context
     * @return true if usage is correct
     */
    private boolean checkUsage(S source, CommandContext<T, S> context) {
        Command<T, S> command = context.command;
        String[] args = context.args;

        int min = command.getArgs().size();
        int max = command.isInfiniteArgs() ? Integer.MAX_VALUE : min + command.getOptionalArgs().size();

        if (args.length < min || args.length > max) {
            String usage = buildUsageMessage(source, context);
            this.sendMessage(source, MessageKey.USAGE, usage, Map.of(MessageKey.USAGE_VALUE, usage));
            return false;
        }
        return true;
    }

    /**
     * Build usage message for command.
     *
     * @param source  the command sender
     * @param context the command context
     * @return the usage message
     */
    private String buildUsageMessage(S source, CommandContext<T, S> context) {
        Command<T, S> command = context.command;
        String label = context.label;

        return command.getUsage().isEmpty()
                ? command.generateDefaultUsage(source, label)
                : command.getUsage();
    }

    /**
     * Execute the command with error handling.
     *
     * @param source  the command sender
     * @param context the command context
     * @return true if execution succeeded or error was handled, false for internal errors
     */
    private boolean executeCommand(S source, CommandContext<T, S> context) {
        try {
            Arguments parsed = manager.parse(context.command, context.args);
            context.command.execute(source, parsed);
            return true;
        } catch (TypeArgumentNotExistException e) {
            return handleTypeArgumentError(source);
        } catch (ArgumentIncorrectException e) {
            return handleArgumentIncorrectError(source, e);
        }
    }

    /**
     * Handle type argument not exist error.
     *
     * @param source the command sender
     * @return false to indicate internal error
     */
    private boolean handleTypeArgumentError(S source) {
        this.sendMessage(source, MessageKey.INTERNAL_ERROR, "&cInternal error: invalid argument type", Map.of());
        return false;
    }

    /**
     * Handle incorrect argument error.
     *
     * @param source the command sender
     * @param e      the exception
     * @return true to indicate error was handled
     */
    private boolean handleArgumentIncorrectError(S source, ArgumentIncorrectException e) {
        String msg = manager.getMessageHandler().getArgNotRecognized().replace("%arg%", e.getInput());
        this.sendMessage(source, MessageKey.ARG_NOT_RECOGNIZED, msg, Map.of(MessageKey.ARG, Objects.toString(e.getInput(), "")));
        return true;
    }

    /**
     * Sends a message to the sender, through the message dispatcher or formatter of the manager.
     * <p>
     * Every message this invoker emits goes through here, so a dispatcher registered with
     * {@link CommandManager#setMessageDispatcher} sees them all by key, and a formatter
     * registered with {@link CommandManager#setMessageFormatter} sees the text of the others,
     * once the internal placeholders have been substituted. Without either, the message is
     * sent as-is.
     * </p>
     *
     * @param source       the command sender
     * @param key          the message
     * @param message      the text of the message
     * @param placeholders the named values of the message
     */
    private void sendMessage(S source, MessageKey key, String message, Map<String, String> placeholders) {
        manager.sendMessage(source, key, message, placeholders);
    }

    /**
     * Suggests command completions based on the provided source, base label, and arguments.
     * This method checks for available tab completers and filters suggestions based on the current input.
     *
     * @param source the command sender (e.g. a player or console)
     * @param base   the command label           (e.g. "hello")
     * @param args   the arguments provided to the command
     * @return the list of suggestion
     */
    public List<String> suggest(S source, String base, String[] args) {
        Optional<MatchResult<T, S>> found = manager.getCommands().findNode(base, args);
        String lastArg = args.length > 0 ? args[args.length - 1] : "";
        if (found.isPresent()) {
            MatchResult<T, S> result = found.get();
            CommandTree.CommandNode<T, S> node = result.node();
            String[] rawArgs = result.args();
            String label = Optional.ofNullable(node.getFullLabel()).orElse(base);
            Map<Integer, TabCompleter<S>> map = manager.getCompleters().get(label);
            if (map != null) {
                TabCompleter<S> completer = map.get(args.length);
                if (completer != null) {
                    return completer.onCompletion(source, Arrays.asList(rawArgs)).stream()
                            .filter(opt -> allowedSuggestion(source, label, opt))
                            .filter(opt -> matchesPrefix(opt, lastArg))
                            .collect(Collectors.toList());
                }
            }
        }

        CommandTree.CommandNode<T, S> current = manager.getCommands().getRoot().getChildren().get(base.toLowerCase());
        if (current == null) return Collections.emptyList();

        current = traverseNode(current, args);
        String parentLabel = current.getFullLabel();

        Stream<String> children = current.getChildren().keySet().stream();
        if (args.length > 0 && current.getChildren().containsKey(lastArg.toLowerCase())) {
            children = children.filter(opt -> matchesPrefix(opt, lastArg));
        }

        return children
                .filter(opt -> allowedSuggestion(source, parentLabel, opt))
                .collect(Collectors.toList());
    }

    private boolean allowedSuggestion(S src, String label, String opt) {
        String full = label + "." + opt.toLowerCase();
        Optional<Command<T, S>> copt = manager.getCommands()
                .findNode(Patterns.DOT.split(full))
                .flatMap(r -> r.node().getCommand());
        if (copt.isEmpty()) return true;
        Command<T, S> c = copt.get();
        return c.getRequirements().stream().allMatch(r -> r.check(src))
                && (c.getPermission().isEmpty() || manager.getPlatform().hasPermission(src, c.getPermission()));
    }

    private CommandTree.CommandNode<T, S> traverseNode(CommandTree.CommandNode<T, S> node, String[] args) {
        int index = 0;
        while (index < args.length - 1) {
            String arg = args[index].toLowerCase();
            CommandTree.CommandNode<T, S> child = node.getChildren().get(arg);
            if (child != null) {
                node = child;
                index++;
            } else {
                break;
            }
        }
        return node;
    }

    private boolean matchesPrefix(String candidate, String current) {
        String lower = current.toLowerCase();
        return candidate.equalsIgnoreCase(current) || candidate.toLowerCase().startsWith(lower);
    }

    /**
     * Internal context class to hold command execution data.
     */
    private record CommandContext<T, S>(Command<T, S> command, String label, String[] args) {
    }
}