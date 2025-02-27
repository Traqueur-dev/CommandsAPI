package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.TypeArgumentNotExistException;
import fr.traqueur.commands.api.requirements.Requirement;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the executor of the commands.
 */
public class Executor<T extends Plugin> implements CommandExecutor, org.bukkit.command.TabCompleter {

    /**
     * The plugin that owns the executor.
     */
    private final T plugin;

    /**
     * The command manager.
     */
    private final CommandManager<T> commandManager;

    /**
     * The constructor of the executor.
     * @param plugin The plugin that owns the executor.
     * @param commandManager The command manager.
     */
    public Executor(T plugin, CommandManager<T> commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
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

    private String parseLabel(String label) {
        String labelLower = label.toLowerCase();
        if(labelLower.contains(":")) {
            String[] split = labelLower.split(":");
            labelLower = split[1];
            if(!split[0].equalsIgnoreCase(plugin.getName().toLowerCase())) {
                return null;
            }
        }
        return labelLower;
    }

    /**
     * This method is called when a command is executed.
     * @param sender The sender of the command.
     * @param command The command executed.
     * @param label The label of the command.
     * @param args The arguments of the command.
     * @return If the command is executed.
     */
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!this.plugin.isEnabled()) {
            return false;
        }

        String labelLower = this.parseLabel(label);

        if(labelLower == null) {
            return false;
        }

        Map<String, Command<T>> commands = this.commandManager.getCommands();

        String cmdLabel = "";
        Command<T> commandFramework = null;
        for (int i = args.length; i >= 0; i--) {
            cmdLabel = getCommandLabel(labelLower, args, i);
            commandFramework = commands.getOrDefault(cmdLabel, null);
            if(commandFramework != null) {
                break;
            }
        }

        if (commandFramework == null) {
            return false;
        }

        if(commandFramework.inGameOnly() && !(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.commandManager.getMessageHandler().getOnlyInGameMessage()));
            return true;
        }

        if (!commandFramework.getPermission().isEmpty() && !sender.hasPermission(commandFramework.getPermission())) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.commandManager.getMessageHandler().getNoPermissionMessage()));
            return true;
        }

        List<Requirement> requirements = commandFramework.getRequirements();
        for (Requirement requirement : requirements) {
            if (!requirement.check(sender)) {
                String error = requirement.errorMessage().isEmpty()
                        ? this.commandManager.getMessageHandler().getRequirementMessage()
                        : requirement.errorMessage();
                error = error.replace("%requirement%", requirement.getClass().getSimpleName());
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', error));
                return true;
            }
        }


        int subCommand = cmdLabel.split("\\.").length - 1;
        String[] modArgs = Arrays.copyOfRange(args, subCommand, args.length);

        if (modArgs.length < commandFramework.getArgs().size()) {
            String usage = commandFramework.getUsage().equalsIgnoreCase("")
                    ? commandFramework.generateDefaultUsage(sender, cmdLabel)
                    : commandFramework.getUsage();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usage));
            return true;
        }

        if (!commandFramework.isInfiniteArgs() && (modArgs.length > commandFramework.getArgs().size() + commandFramework.getOptinalArgs().size())) {
            String usage = commandFramework.getUsage().equalsIgnoreCase("")
                    ? commandFramework.generateDefaultUsage(sender, cmdLabel)
                    : commandFramework.getUsage();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usage));
            return true;
        }

        try {
            Arguments arguments = this.commandManager.parse(commandFramework, modArgs);
            commandFramework.execute(sender, arguments);
        } catch (TypeArgumentNotExistException e) {
            throw new RuntimeException(e);
        } catch (ArgumentIncorrectException e) {
            String message = this.commandManager.getMessageHandler().getArgNotRecognized();
            message = message.replace("%arg%", e.getInput());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }

        return true;
    }

    /**
     * This method is called when a tab is completed.
     * @param commandSender The sender of the command.
     * @param command The command completed.
     * @param label The label of the command.
     * @param args The arguments of the command.
     * @return The list of completions.
     */
    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String label, String[] args) {
        String arg = args[args.length-1];

        String labelLower = this.parseLabel(label);
        if(labelLower == null) {
            return Collections.emptyList();
        }

        Map<String, Map<Integer, TabCompleter>> completers = this.commandManager.getCompleters();
        Map<String, Command<T>> commands = this.commandManager.getCommands();

        String cmdLabel = "";
        Map<Integer, TabCompleter> map = null;
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

        TabCompleter converter = map.get(args.length);
        String argsBeforeString = (label +
                "." +
                String.join(".", Arrays.copyOf(args, args.length - 1)))
                .replaceFirst("^" + cmdLabel + "\\.", "");

        List<String> completer = converter.onCompletion(commandSender, Arrays.asList(argsBeforeString.split("\\."))).stream()
                .filter(str -> str.toLowerCase().startsWith(arg.toLowerCase()) || str.equalsIgnoreCase(arg))
                .collect(Collectors.toList());

        String finalCmdLabel = cmdLabel;
        return completer.stream().filter(str -> {
            String cmdLabelInner = finalCmdLabel + "." + str.toLowerCase();
            if(commands.containsKey(cmdLabelInner)) {
                Command<?> frameworkCommand = commands.get(cmdLabelInner);
                List<Requirement> requirements = frameworkCommand.getRequirements();
                for (Requirement requirement : requirements) {
                    if (!requirement.check(commandSender)) {
                        return false;
                    }
                }
                return frameworkCommand.getPermission().isEmpty() || commandSender.hasPermission(frameworkCommand.getPermission());
            }
            return true;
        }).collect(Collectors.toList());

    }

}
