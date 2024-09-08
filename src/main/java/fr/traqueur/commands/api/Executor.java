package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.TabConverter;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.TypeArgumentNotExistException;
import fr.traqueur.commands.api.logging.Messages;
import fr.traqueur.commands.api.requirements.Requirement;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the executor of the commands.
 */
public class Executor implements CommandExecutor, TabCompleter {

    /**
     * The plugin that owns the executor.
     */
    private final JavaPlugin plugin;

    /**
     * The command manager.
     */
    private final CommandManager commandManager;

    /**
     * The commands registered.
     */
    private final Map<String, fr.traqueur.commands.api.Command<?>> commands;

    /**
     * The completers registered.
     */
    private final Map<String, Map<Integer, TabConverter>> completers;

    /**
     * The constructor of the executor.
     * @param plugin The plugin that owns the executor.
     * @param commandManager The command manager.
     */
    public Executor(JavaPlugin plugin, CommandManager commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
        this.commands = this.commandManager.getCommands();
        this.completers = this.commandManager.getCompleters();
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

        if (!command.testPermission(sender)) {
            return true;
        }

        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());
            for (int x = 0; x < i; x++) {
                String component = args[x];
                if(args[x].contains(":") && x == 0) {
                    String[] split = args[x].split(":");
                    component = split[1];
                    if(!split[0].equals(plugin.getName())) {
                        return false;
                    }
                }

                buffer.append(".").append(component.toLowerCase());
            }
            String cmdLabel = buffer.toString();
            if (commands.containsKey(cmdLabel)) {
                fr.traqueur.commands.api.Command<?> commandFramework = commands.get(cmdLabel);
                if (!commandFramework.getPermission().isEmpty() && !sender.hasPermission(commandFramework.getPermission())) {
                    sender.sendMessage(Messages.NO_PERMISSION.message());
                    return true;
                }
                if (commandFramework.inGameOnly() && !(sender instanceof Player)) {
                    sender.sendMessage(Messages.ONLY_IN_GAME.message());
                    return true;
                }
                int subCommand = cmdLabel.split("\\.").length - 1;
                String[] modArgs = new String[args.length - subCommand];
                if (args.length - subCommand >= 0)
                    System.arraycopy(args, subCommand, modArgs, 0, args.length - subCommand);

                if (modArgs.length < commandFramework.getArgs().size()) {
                    String usage = command.getUsage();
                    if (usage.isEmpty()) {
                        usage = Messages.MISSING_ARGS.message();
                    }
                    sender.sendMessage(usage);
                    return true;
                }

                if (!commandFramework.isInfiniteArgs() && (modArgs.length > commandFramework.getArgs().size() + commandFramework.getOptinalArgs().size())) {
                    String usage = command.getUsage();
                    if (usage.isEmpty()) {
                        usage = Messages.MISSING_ARGS.message();
                    }
                    sender.sendMessage(usage);
                    return true;
                }

                Arguments arguments;
                try {
                    arguments = this.commandManager.parse(commandFramework, modArgs);
                } catch (TypeArgumentNotExistException e) {
                    throw new RuntimeException(e);
                } catch (ArgumentIncorrectException e) {
                    String message = Messages.ARG_NOT_RECOGNIZED.message();
                    message = message.replace("%arg%", e.getInput());
                    sender.sendMessage( message);
                    return true;
                }

                List<Requirement> requirements = commandFramework.getRequirements();
                for (Requirement requirement : requirements) {
                    if (!requirement.check(sender)) {
                        String error = requirement.errorMessage().isEmpty()
                                ? Messages.REQUIREMENT_ERROR.message()
                                : ChatColor.translateAlternateColorCodes('&', requirement.errorMessage());
                        error = error.replace("%requirement%", requirement.getClass().getSimpleName());
                        sender.sendMessage(error);
                        return true;
                    }
                }

                commandFramework.execute(sender, arguments);
                return true;
            }
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
        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());
            for (int x = 0; x < i; x++) {
                buffer.append(".").append(args[x].toLowerCase());
            }
            String cmdLabel = buffer.toString();
            if (this.completers.containsKey(cmdLabel)) {
                Map<Integer, TabConverter> map = this.completers.get(cmdLabel);
                if(map.containsKey(args.length)) {
                    TabConverter converter = map.get(args.length);
                    List<String> completer = converter.onCompletion(commandSender).stream().filter(str -> str.toLowerCase().startsWith(arg.toLowerCase()) || str.equalsIgnoreCase(arg)).toList();
                    return completer.stream().filter(str -> {
                        String cmdLabelInner = cmdLabel + "." + str.toLowerCase();
                        if(this.commands.containsKey(cmdLabelInner)) {
                            fr.traqueur.commands.api.Command<?> frameworkCommand = this.commands.get(cmdLabelInner);
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
        }

        return List.of();
    }

}
