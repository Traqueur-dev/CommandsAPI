package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.TypeArgumentNotExistException;
import fr.traqueur.commands.api.requirements.Requirement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The command executor for Velocity.
 * This class implements the RawCommand interface and handles command execution and suggestions.
 *
 * @param <T> The type of the command manager.
 */
public class Executor<T> implements RawCommand {

    /**
     * The serializer used to convert legacy components to Adventure components.
     */
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    /**
     * The MiniMessage instance used for parsing messages.
     */
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * The command manager that this executor uses to manage commands.
     */
    private final CommandManager<T, CommandSource> manager;

    /**
     * Constructs a new Executor with the given command manager.
     *
     * @param manager The command manager to use for this executor.
     */
    public Executor(CommandManager<T, CommandSource> manager) {
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
        String label = invocation.alias(); // base command (e.g. "test")

        String labelLower = label.toLowerCase();

        Map<String, Command<T, CommandSource>> commands = this.manager.getCommands();

        String cmdLabel = "";
        Command<T, CommandSource> commandFramework = null;
        for (int i = args.length; i >= 0; i--) {
            cmdLabel = getCommandLabel(labelLower, args, i);
            commandFramework = commands.getOrDefault(cmdLabel, null);
            if(commandFramework != null) {
                break;
            }
        }

        if (commandFramework == null) {
            return;
        }

        if(commandFramework.inGameOnly() && !(source instanceof Player)) {
            source.sendMessage(this.parse(this.manager.getMessageHandler().getOnlyInGameMessage()));
            return;
        }

        if (!commandFramework.getPermission().isEmpty() && !source.hasPermission(commandFramework.getPermission())) {
            source.sendMessage(this.parse(this.manager.getMessageHandler().getNoPermissionMessage()));
            return;
        }

        List<Requirement<CommandSource>> requirements = commandFramework.getRequirements();
        for (Requirement<CommandSource> requirement : requirements) {
            if (!requirement.check(source)) {
                String error = requirement.errorMessage().isEmpty()
                        ? this.manager.getMessageHandler().getRequirementMessage()
                        : requirement.errorMessage();
                error = error.replace("%requirement%", requirement.getClass().getSimpleName());
                source.sendMessage(this.parse(error));
                return;
            }
        }

        int subCommand = cmdLabel.split("\\.").length - 1;
        String[] modArgs = Arrays.copyOfRange(args, subCommand, args.length);

        if (modArgs.length < commandFramework.getArgs().size()) {
            String usage = commandFramework.getUsage().equalsIgnoreCase("")
                    ? commandFramework.generateDefaultUsage(this.manager.getPlatform(), source, cmdLabel)
                    : commandFramework.getUsage();
            source.sendMessage(this.parse(usage));
            return;
        }

        if (!commandFramework.isInfiniteArgs() && (modArgs.length > commandFramework.getArgs().size() + commandFramework.getOptinalArgs().size())) {
            String usage = commandFramework.getUsage().equalsIgnoreCase("")
                    ? commandFramework.generateDefaultUsage(this.manager.getPlatform(), source, cmdLabel)
                    : commandFramework.getUsage();
            source.sendMessage(this.parse(usage));
            return;
        }

        try {
            Arguments arguments = this.manager.parse(commandFramework, modArgs);
            commandFramework.execute(new VelocityCommandContext(source, arguments));
        } catch (TypeArgumentNotExistException e) {
            throw new RuntimeException(e);
        } catch (ArgumentIncorrectException e) {
            String message = this.manager.getMessageHandler().getArgNotRecognized();
            message = message.replace("%arg%", e.getInput());
            source.sendMessage(this.parse(message));
        }
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
        String arg = args[args.length-1];
        String labelLower = label.toLowerCase();

        Map<String, Map<Integer, TabCompleter<CommandSource>>> completers = this.manager.getCompleters();
        Map<String, Command<T, CommandSource>> commands = this.manager.getCommands();

        String cmdLabel = "";
        Map<Integer, TabCompleter<CommandSource>> map = null;
        for (int i = args.length; i >= 0; i--) {
            cmdLabel = getCommandLabel(labelLower, args, i);
            map = completers.getOrDefault(cmdLabel, null);
            if(map != null) {
                break;
            }
        }

        if (map == null || !map.containsKey(args.length)) {
            return Collections.emptyList();
        }
        TabCompleter<CommandSource> converter = map.get(args.length);
        String argsBeforeString = (label +
                "." +
                String.join(".", Arrays.copyOf(args, args.length - 1)))
                .replaceFirst("^" + cmdLabel + "\\.", "");

        List<String> completer = converter.onCompletion(new VelocityTabContext(source, Arrays.asList(argsBeforeString.split("\\."))))
                .stream()
                .filter(str -> str.toLowerCase().startsWith(arg.toLowerCase()) || str.equalsIgnoreCase(arg))
                .toList();

        String finalCmdLabel = cmdLabel;
        return completer.stream().filter(str -> {
            String cmdLabelInner = finalCmdLabel + "." + str.toLowerCase();
            if(commands.containsKey(cmdLabelInner)) {
                Command<?, CommandSource> frameworkCommand = commands.get(cmdLabelInner);
                List<Requirement<CommandSource>> requirements = frameworkCommand.getRequirements();
                for (Requirement<CommandSource> requirement : requirements) {
                    if (!requirement.check(source)) {
                        return false;
                    }
                }
                return frameworkCommand.getPermission().isEmpty() || source.hasPermission(frameworkCommand.getPermission());
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Parses a message from legacy format to Adventure format.
     *
     * @param message The message in legacy format.
     * @return The parsed message in Adventure format.
     */
    private Component parse(String message) {
        Component legacy = SERIALIZER.deserialize(message);
        String asMini = MINI_MESSAGE.serialize(legacy);
        return MINI_MESSAGE.deserialize(asMini);
    }

    /**
     * Constructs a command label by appending the arguments to the base label.
     *
     * @param label The base command label.
     * @param args The arguments to append.
     * @param commandLabelSize The number of arguments to include in the label.
     * @return The constructed command label.
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
}