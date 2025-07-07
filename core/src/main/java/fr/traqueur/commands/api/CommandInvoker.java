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
 * CommandInvoker is responsible for invoking commands based on the provided label and arguments.
 * It checks permissions, requirements, and executes the command if all conditions are met.
 *
 * @param <T> The type of the command sender (e.g., Player, Console).
 * @param <S> The type of the source (e.g., Player, CommandSender).
 */
public class CommandInvoker<T, S> {

    /**
     * The command manager that holds the commands and their configurations.
     */
    private final CommandManager<T, S> commandManager;

    /**
     * Constructor for CommandInvoker.
     * @param manager The command manager that holds the commands and their configurations.
     */
    public CommandInvoker(CommandManager<T,S> manager) {
        this.commandManager = manager;
    }

    /**
     * Get the command label from the label and the arguments.
     * @param label The label of the command.
     * @param args The arguments of the command.
     * @param commandLabelSize The size of the command label.
     * @return The command label.
     */
    private String getCommandLabel(String label, String[] args, int commandLabelSize) {
        StringBuilder buffer = new StringBuilder();
        String labelLower = label.toLowerCase();
        buffer.append(labelLower);
        for (int x = 0; x <  commandLabelSize; x++) {
            buffer.append(".").append(args[x].toLowerCase());
        }
        return buffer.toString();
    }

    /**
     * Invokes a command based on the provided source, base label, and raw arguments.
     * It checks for command existence, permissions, requirements, and executes the command if valid.
     *
     * @param source The source of the command (e.g., Player, Console).
     * @param baseLabel The base label of the command.
     * @param rawArgs The raw arguments passed to the command.
     * @return true if the command was successfully invoked, false otherwise.
     */
    public boolean invoke(S source,
                       String baseLabel,
                       String[] rawArgs) {

        Map<String, Command<T, S>> commands = this.commandManager.getCommands();

        String cmdLabel = "";
        Command<T, S> commandFramework = null;
        for (int i = rawArgs.length; i >= 0; i--) {
            cmdLabel = getCommandLabel(baseLabel, rawArgs, i);
            commandFramework = commands.getOrDefault(cmdLabel, null);
            if(commandFramework != null) {
                break;
            }
        }

        if (commandFramework == null) {
            return false;
        }

        if(commandFramework.inGameOnly() && ! this.commandManager.getPlatform().isPlayer(source)) {
            this.commandManager.getPlatform().sendMessage(source, this.commandManager.getMessageHandler().getOnlyInGameMessage());
            return true;
        }

        if (!commandFramework.getPermission().isEmpty() && !this.commandManager.getPlatform().hasPermission(source, commandFramework.getPermission())) {
            this.commandManager.getPlatform().sendMessage(source, this.commandManager.getMessageHandler().getNoPermissionMessage());
            return true;
        }

        List<Requirement<S>> requirements = commandFramework.getRequirements();
        for (Requirement<S> requirement : requirements) {
            if (!requirement.check(source)) {
                String error = requirement.errorMessage().isEmpty()
                        ? this.commandManager.getMessageHandler().getRequirementMessage()
                        : requirement.errorMessage();
                error = error.replace("%requirement%", requirement.getClass().getSimpleName());
                this.commandManager.getPlatform().sendMessage(source, error);
                return true;
            }
        }


        int subCommand = cmdLabel.split("\\.").length - 1;
        String[] modArgs = Arrays.copyOfRange(rawArgs, subCommand, rawArgs.length);

        if ((modArgs.length < commandFramework.getArgs().size()) || (!commandFramework.isInfiniteArgs() && (modArgs.length > commandFramework.getArgs().size() + commandFramework.getOptinalArgs().size()))) {
            String usage = commandFramework.getUsage().equalsIgnoreCase("")
                    ? commandFramework.generateDefaultUsage(this.commandManager.getPlatform(), source, cmdLabel)
                    : commandFramework.getUsage();
            this.commandManager.getPlatform().sendMessage(source, usage);
            return true;
        }

        try {
            Arguments arguments = this.commandManager.parse(commandFramework, modArgs);
            commandFramework.execute(source, arguments);
        } catch (TypeArgumentNotExistException e) {
            throw new RuntimeException(e);
        } catch (ArgumentIncorrectException e) {
            String message = this.commandManager.getMessageHandler().getArgNotRecognized();
            message = message.replace("%arg%", e.getInput());
            this.commandManager.getPlatform().sendMessage(source, message);
        }
        return true;
    }

    /**
     * Suggests completions for the command based on the provided source, label, and arguments.
     * It filters the suggestions based on the current argument and checks permissions for each suggestion.
     *
     * @param source The source of the command (e.g., Player, Console).
     * @param label The label of the command.
     * @param args The arguments passed to the command.
     * @return A list of suggested completions.
     */
    public List<String> suggest(S source, String label, String[] args) {
        String arg = args[args.length-1];


        Map<String, Map<Integer, TabCompleter<S>>> completers = commandManager.getCompleters();
        Map<String, Command<T, S>> commands = this.commandManager.getCommands();

        String cmdLabel = "";
        Map<Integer, TabCompleter<S>> map = null;
        for (int i = args.length; i >= 0; i--) {
            cmdLabel = getCommandLabel(label, args, i);
            map = completers.getOrDefault(cmdLabel, null);
            if(map != null) {
                break;
            }
        }

        if (map == null || !map.containsKey(args.length)) {
            return Collections.emptyList();
        }

        TabCompleter<S> converter = map.get(args.length);
        String argsBeforeString = (label +
                "." +
                String.join(".", Arrays.copyOf(args, args.length - 1)))
                .replaceFirst("^" + cmdLabel + "\\.", "");

        List<String> completer = converter.onCompletion(source, Arrays.asList(argsBeforeString.split("\\.")))
                .stream()
                .filter(str -> str.toLowerCase().startsWith(arg.toLowerCase()) || str.equalsIgnoreCase(arg))
                .collect(Collectors.toList());

        String finalCmdLabel = cmdLabel;
        return completer.stream().filter(str -> {
            String cmdLabelInner = finalCmdLabel + "." + str.toLowerCase();
            if(commands.containsKey(cmdLabelInner)) {
                Command<T, S> frameworkCommand = commands.get(cmdLabelInner);
                List<Requirement<S>> requirements = frameworkCommand.getRequirements();
                for (Requirement<S> requirement : requirements) {
                    if (!requirement.check(source)) {
                        return false;
                    }
                }
                return frameworkCommand.getPermission().isEmpty() || this.commandManager.getPlatform().hasPermission(source, frameworkCommand.getPermission());
            }
            return true;
        }).collect(Collectors.toList());
    }
}
