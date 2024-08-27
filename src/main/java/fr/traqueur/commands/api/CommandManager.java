package fr.traqueur.commands.api;

import com.google.common.collect.Lists;
import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabConverter;
import fr.traqueur.commands.api.logging.Logger;
import fr.traqueur.commands.impl.arguments.*;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.TypeArgumentNotExistException;
import fr.traqueur.commands.api.logging.MessageHandler;
import fr.traqueur.commands.api.logging.Messages;
import fr.traqueur.commands.impl.logging.InternalLogger;
import fr.traqueur.commands.impl.logging.InternalMessageHandler;
import fr.traqueur.commands.api.requirements.Requirement;
import fr.traqueur.commands.api.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is the command manager.
 * It allows you to register commands and subcommands.
 * It also allows you to register argument converters and tab completers.
 */
public class CommandManager {

    private static final String TYPE_PARSER = ":";
    private static final String INFINITE = "infinite";

    /**
     * The instance of the command manager. Only for internal use.
     */
    private static CommandManager instance;

    /**
     * The plugin that owns the command manager.
     */
    private final Plugin plugin;

    /**
     * The command map of the server.
     */
    private final CommandMap commandMap;

    /**
     * The constructor of the plugin command.
     */
    private final Constructor<? extends PluginCommand> pluginConstructor;

    /**
     * The commands registered in the command manager.
     */
    private final Map<String, Command<?>> commands;

    /**
     * The argument converters registered in the command manager.
     */
    private final Map<String, Map.Entry<Class<?>, ArgumentConverter<?>>> typeConverters;

    /**
     * The tab completers registered in the command manager.
     */
    private final Map<String, Map<Integer, TabConverter>> completers;

    /**
     * The executor of the command manager.
     */
    private final Executor executor;


    /**
     * The logger of the command manager.
     */
    private Logger logger;
    /**
     * The constructor of the command manager.
     * @param plugin The plugin that owns the command manager.
     */
    public CommandManager(JavaPlugin plugin) {
        Updater.checkUpdates();
        Messages.setMessageHandler(new InternalMessageHandler());
        this.logger = new InternalLogger(plugin.getLogger());

        instance = this;

        this.plugin = plugin;
        this.commands = new HashMap<>();
        this.typeConverters = new HashMap<>();
        this.completers = new HashMap<>();
        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            pluginConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginConstructor.setAccessible(true);
        } catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        this.registerConverter(String.class, "string", (s) -> s);
        this.registerConverter(Boolean.class, "boolean", new BooleanArgument());
        this.registerConverter(Integer.class, "int",new IntegerArgument());
        this.registerConverter(Double.class, "double",new DoubleArgument());
        this.registerConverter(Long.class, "long", new LongArgument());
        this.registerConverter(Player.class, "player", new PlayerArgument());
        this.registerConverter(OfflinePlayer.class, "offlineplayer", new OfflinePlayerArgument());
        this.registerConverter(String.class, INFINITE, s -> s);

        this.executor = new Executor(plugin, this);
    }

    /**
     * Set the custom logger of the command manager.
     * @param logger The logger to set.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Set the message handler of the command manager.
     * @param messageHandler The message handler to set.
     */
    public void setMessageHandler(MessageHandler messageHandler) {
        Messages.setMessageHandler(messageHandler);
    }


    /**
     * Register a command in the command manager.
     * @param command The command to register.
     */
    public void registerCommand(Command<?> command) {
        try {
            List<String> aliases = new ArrayList<>(command.getAliases());
            aliases.add(command.getName());
            for (String alias : aliases) {
                this.addCommand(command, alias);
                this.registerSubCommands(alias, command.getSubcommands());
            }
        } catch(TypeArgumentNotExistException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unregister a command in the command manager.
     * @param label The label of the command to unregister.
     */
    public void unregisterCommand(String label) {
        this.unregisterCommand(label, true);
    }

    /**
     * Unregister a command in the command manager.
     * @param label The label of the command to unregister.
     * @param subcommands If the subcommands must be unregistered.
     */
    public void unregisterCommand(String label, boolean subcommands) {
        if(this.commands.get(label) == null) {
            throw new IllegalArgumentException("The command " + label + " does not exist.");
        }
        this.unregisterCommand(this.commands.get(label), subcommands);
    }

    /**
     * Unregister a command in the command manager.
     * @param command The command to unregister.
     */
    public void unregisterCommand(Command<?> command) {
        this.unregisterCommand(command, true);
    }

    /**
     * Unregister a command in the command manager.
     * @param command The command to unregister.
     * @param subcommands If the subcommands must be unregistered.
     */
    public void unregisterCommand(Command<?> command, boolean subcommands) {
        List<String> aliases = new ArrayList<>(command.getAliases());
        aliases.add(command.getName());
        for (String alias : aliases) {
            this.removeCommand(alias, subcommands);
            if(subcommands) {
                this.unregisterSubCommands(alias, command.getSubcommands());
            }
        }
    }

    /**
     * Register an argument converter in the command manager.
     * @param typeClass The class of the type.
     * @param type The type of the argument.
     * @param converter The converter of the argument.
     * @param <T> The type of the argument.
     */
    public <T> void registerConverter(Class<T> typeClass, String type, ArgumentConverter<T> converter) {
        this.typeConverters.put(type, new AbstractMap.SimpleEntry<>(typeClass, converter));
    }

    /**
     * Register a list of subcommands in the command manager.
     * @param parentLabel The parent label of the commands.
     * @param subcommands The list of subcommands to register.
     * @throws TypeArgumentNotExistException If the type of the argument does not exist.
     */
    private void registerSubCommands(String parentLabel, List<Command<?>> subcommands) throws TypeArgumentNotExistException {
        if(subcommands == null || subcommands.isEmpty()) {
            return;
        }
        for (Command<?> subcommand : subcommands) {
            List<String> aliasesSub = new ArrayList<>(subcommand.getAliases());
            aliasesSub.add(subcommand.getName());
            for (String aliasSub : aliasesSub) {
                this.addCommand(subcommand, parentLabel + "." + aliasSub);
                this.registerSubCommands(parentLabel + "." + aliasSub, subcommand.getSubcommands());
            }
        }
    }

    /**
     * Unregister the subcommands of a command.
     * @param parentLabel The parent label of the subcommands.
     * @param subcommandsList The list of subcommands to unregister.
     */
    private void unregisterSubCommands(String parentLabel, List<Command<?>> subcommandsList) {
        if(subcommandsList == null || subcommandsList.isEmpty()) {
            return;
        }
        for (Command<?> subcommand : subcommandsList) {
            List<String> aliasesSub = new ArrayList<>(subcommand.getAliases());
            aliasesSub.add(subcommand.getName());
            for (String aliasSub : aliasesSub) {
                this.removeCommand(parentLabel + "." + aliasSub, true);
                this.unregisterSubCommands(parentLabel + "." + aliasSub, subcommand.getSubcommands());
            }
        }
    }

    /**
     * Unregister a command in the command manager.
     * @param label The label of the command.
     * @param subcommand If the subcommand must be unregistered.
     */
    private void removeCommand(String label, boolean subcommand) {
        if(subcommand && this.commandMap.getCommand(label) != null) {
            Objects.requireNonNull(this.commandMap.getCommand(label)).unregister(commandMap);
        }
        this.commands.remove(label);
        this.completers.remove(label);
    }

    /**
     * Register a command in the command manager.
     * @param command The command to register.
     * @param label The label of the command.
     * @throws TypeArgumentNotExistException If the type of the argument does not exist.
     */
    private void addCommand(Command<?> command, String label) throws TypeArgumentNotExistException {
        try {
            this.logger.info("Register command " + label);
            List<Argument> args = command.getArgs();
            List<Argument> optArgs = command.getOptinalArgs();
            String[] labelParts = label.split("\\.");
            String cmdLabel = labelParts[0].toLowerCase();
            int labelSize = labelParts.length;

            if(!this.checkTypeForArgs(args) || !this.checkTypeForArgs(optArgs)) {
                throw new TypeArgumentNotExistException();
            }

            commands.put(label.toLowerCase(), command);

            String originCmdLabel = cmdLabel;
            for (Command<?> value : commands.values().stream().filter(commandInner -> !commandInner.isSubCommand()).toList()) {
                if(value.getAliases().contains(cmdLabel)) {
                    originCmdLabel = value.getName();
                }
            }

            if (commandMap.getCommand(originCmdLabel) == null) {
                PluginCommand cmd = pluginConstructor.newInstance(originCmdLabel, command.getPlugin());

                cmd.setExecutor(this.executor);
                cmd.setTabCompleter(this.executor);
                cmd.setAliases(command.getAliases());

                if(!commandMap.register(originCmdLabel, this.plugin.getName(), cmd)) {
                    this.logger.error("Unable to add the command " + originCmdLabel);
                    return;
                }
            }
            if (!command.getDescription().equalsIgnoreCase("") && cmdLabel.equals(label)) {
                Objects.requireNonNull(commandMap.getCommand(originCmdLabel)).setDescription(command.getDescription());
            }
            if (!command.getUsage().equalsIgnoreCase("") && cmdLabel.equals(label)) {
                Objects.requireNonNull(commandMap.getCommand(originCmdLabel)).setUsage(command.getUsage());
            }

            this.addCompletionsForLabel(labelParts);
            this.addCompletionForArgs(label, labelSize, args);
            this.addCompletionForArgs(label, labelSize + args.size(), optArgs);

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register the completions of the command.
     * @param labelParts The parts of the label.
     */
    private void addCompletionsForLabel(String[] labelParts) {
        StringBuilder currentLabel = new StringBuilder();
        for (int i = 0; i < labelParts.length - 1; i++) {
            if (i > 0) {
                currentLabel.append(".");
            }
            currentLabel.append(labelParts[i]);
            String completionPart = labelParts[i + 1];
            this.addCompletion(currentLabel.toString(), i + 1, (sender) -> Lists.newArrayList(completionPart));
        }
    }

    /**
     * Register the completions of the arguments.
     * @param label The label of the command.
     * @param commandSize The size of the command.
     * @param args The arguments to register.
     */
    private void addCompletionForArgs(String label, int commandSize, List<Argument> args) {
        for (int i = 0; i < args.size(); i++) {
            Argument arg = args.get(i);
            String[] parts = arg.arg().split(TYPE_PARSER);
            String type = parts[1].trim();
            ArgumentConverter<?> converter = this.typeConverters.get(type).getValue();
            TabConverter argConverter = arg.tabConverter();
            if (argConverter != null) {
                this.addCompletion(label,commandSize + i, argConverter);
            } else if (converter instanceof TabConverter tabConverter) {
                this.addCompletion(label,commandSize + i, tabConverter);
            } else {
                this.addCompletion(label, commandSize + i, (sender) -> new ArrayList<>());
            }
        }
    }

    /**
     * Register a tab completer in the command manager.
     * @param label The label of the command.
     * @param commandSize The size of the command.
     * @param converter The converter of the tab completer.
     */
    private void addCompletion(String label, int commandSize, TabConverter converter) {
        Map<Integer, TabConverter> mapInner = this.completers.getOrDefault(label, new HashMap<>());
        TabConverter newConverter;
        TabConverter converterInner = mapInner.getOrDefault(commandSize, null);
        if(converterInner != null) {
            newConverter = (sender) -> {
                List<String> completions = new ArrayList<>(converterInner.onCompletion(sender));
                completions.addAll(converter.onCompletion(sender));
                return completions;
            };
        } else {
            newConverter = converter;
        }
        mapInner.put(commandSize, newConverter);
        this.completers.put(label, mapInner);
    }

    /**
     * Check if the type of the argument exists.
     * @param args The arguments to check.
     */
    private boolean checkTypeForArgs(List<Argument> args) throws TypeArgumentNotExistException {
        for(String arg: args.stream().map(Argument::arg).toList()) {
            String[] parts = arg.split(TYPE_PARSER);

            if (parts.length != 2) {
                throw new TypeArgumentNotExistException();
            }
            String type = parts[1].trim();
            if(!this.typeExist(type)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the type of the argument exists.
     * @param type The type of the argument.
     * @return If the type of the argument exists.
     */
    private boolean typeExist(String type) {
        return this.typeConverters.containsKey(type);
    }

    /**
     * Parse the arguments of the command.
     * @param command The command to parse.
     * @param args The arguments to parse.
     * @return The arguments parsed.
     * @throws TypeArgumentNotExistException If the type of the argument does not exist.
     * @throws ArgumentIncorrectException If the argument is incorrect.
     */
    protected Arguments parse(Command<?> command, String[] args) throws TypeArgumentNotExistException, ArgumentIncorrectException {
        Arguments arguments = new Arguments(this.logger);
        List<Argument> templates = command.getArgs();
        for (int i = 0; i < templates.size(); i++) {
            String input = args[i];
            if (applyParsing(args, arguments, templates, i, input)) break;
        }

        List<Argument> optArgs = command.getOptinalArgs();
        if (optArgs.isEmpty()) {
            return arguments;
        }

        for (int i = 0; i < optArgs.size(); i++) {
            if (args.length > templates.size() + i) {
                String input = args[templates.size() + i];
                if (applyParsing(args, arguments, optArgs, i, input)) break;
            }
        }

        return arguments;
    }

    /**
     * Apply the parsing of the arguments.
     * @param args The arguments to parse.
     * @param arguments The arguments parsed.
     * @param templates The templates of the arguments.
     * @param argIndex The index of the argument.
     * @param input The input of the argument.
     * @return  If the parsing is applied.
     * @throws TypeArgumentNotExistException If the type of the argument does not exist.
     * @throws ArgumentIncorrectException If the argument is incorrect.
     */
    private boolean applyParsing(String[] args, Arguments arguments, List<Argument> templates, int argIndex,
                                 String input) throws TypeArgumentNotExistException, ArgumentIncorrectException {
        String template = templates.get(argIndex).arg();
        String[] parts = template.split(TYPE_PARSER);

        if (parts.length != 2) {
            throw new TypeArgumentNotExistException();
        }

        String key = parts[0].trim();
        String type = parts[1].trim();

        if (type.equals(INFINITE)) {
            StringBuilder builder = new StringBuilder();
            for (int i = argIndex; i < args.length; i++) {
                builder.append(args[i]);
                if (i < args.length - 1) {
                    builder.append(" ");
                }
            }
            arguments.add(key, String.class, builder.toString());
            return true;
        }

        if (typeConverters.containsKey(type)) {
            Class<?> typeClass = typeConverters.get(type).getKey();
            ArgumentConverter<?> converter = typeConverters.get(type).getValue();
            Object obj = converter.apply(input);
            if (obj == null) {
                throw new ArgumentIncorrectException(input);
            }
            arguments.add(key, typeClass, obj);
        }
        return false;
    }

    /**
     * Get the commands of the command manager.
     * @return The commands of the command manager.
     */
    public Map<String, Command<?>> getCommands() {
        return commands;
    }


    /**
     * Get the completers of the command manager
     * @return The completers of command manager
     */
    public Map<String, Map<Integer, TabConverter>> getCompleters() {
        return this.completers;
    }

    /**
     * Get the instance of the command manager. Only for internal use, it's why it's protected.
     * @return The instance of the command manager.
     */
    protected static CommandManager getInstance() {
        return instance;
    }
}
