package fr.traqueur.commands.velocity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
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

public class Executor<T> implements RawCommand {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final CommandManager<T, CommandSource> manager;

    public Executor(CommandManager<T, CommandSource> manager) {
        this.manager = manager;
    }

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

    private Component parse(String message) {
        Component legacy = SERIALIZER.deserialize(message);
        String asMini = MINI_MESSAGE.serialize(legacy);
        return MINI_MESSAGE.deserialize(asMini);
    }

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