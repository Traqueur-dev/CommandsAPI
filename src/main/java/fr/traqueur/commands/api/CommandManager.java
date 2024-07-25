package fr.traqueur.commands.api;

import com.google.common.collect.Lists;
import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabConverter;
import fr.traqueur.commands.api.arguments.impl.*;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.TypeArgumentNotExistException;
import fr.traqueur.commands.api.lang.InternalMessageHandler;
import fr.traqueur.commands.api.lang.Lang;
import fr.traqueur.commands.api.lang.Messages;
import fr.traqueur.commands.api.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

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
public class CommandManager implements CommandExecutor, TabCompleter {

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
     * The constructor of the command manager.
     * @param plugin The plugin that owns the command manager.
     */
    public CommandManager(JavaPlugin plugin) {
        Updater.checkUpdates();

//        plugin.saveResource("commands.yml", false);
//        Lang.setupMessages(plugin);
        Lang.setMessageHandler(new InternalMessageHandler());

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
        this.registerConverter(Integer.class, "int",new IntegerArgument());
        this.registerConverter(Double.class, "double",new DoubleArgument());
        this.registerConverter(Long.class, "long", new LongArgument());
        this.registerConverter(Player.class, "player", new PlayerArgument());
        this.registerConverter(OfflinePlayer.class, "offlineplayer", new OfflinePlayerArgument());
        this.registerConverter(String.class, "infinite", s -> s);
    }



    /**
     * Register a command in the command manager.
     * @param command The command to register.
     */
    public void registerCommand(Command<?> command) {
        try {
            ArrayList<String> aliases = new ArrayList<>(command.getAliases());
            aliases.add(command.getName());
            for (String alias : aliases) {
                this.registerCommand(command, alias);
                this.registerSubCommands(alias, command.getSubcommands());
            }
        } catch(TypeArgumentNotExistException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register a list of subcommands in the command manager.
     * @param parentLabel The parent label of the commands.
     * @param subcommands The list of subcommands to register.
     * @throws TypeArgumentNotExistException If the type of the argument does not exist.
     */
    public void registerSubCommands(String parentLabel, List<Command<?>> subcommands) throws TypeArgumentNotExistException {
        if(subcommands == null || subcommands.isEmpty()) {
            return;
        }
        for (Command<?> subcommand : subcommands) {
            ArrayList<String> aliasesSub = new ArrayList<>(subcommand.getAliases());
            aliasesSub.add(subcommand.getName());
            for (String aliasSub : aliasesSub) {
                this.registerCommand(subcommand, parentLabel + "." + aliasSub);
                this.registerSubCommands(parentLabel + "." + aliasSub, subcommand.getSubcommands());
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
     * Register a command in the command manager.
     * @param command The command to register.
     * @param label The label of the command.
     * @throws TypeArgumentNotExistException If the type of the argument does not exist.
     */
    private void registerCommand(Command<?> command, String label) throws TypeArgumentNotExistException {
        try {
            plugin.getLogger().info("Register command " + label);
            ArrayList<Argument> args = command.getArgs();
            ArrayList<Argument> optArgs = command.getOptinalArgs();

            if(!this.checkTypeForArgs(args) || !this.checkTypeForArgs(optArgs)) {
                throw new TypeArgumentNotExistException();
            }
            commands.put(label.toLowerCase(), command);
            String cmdLabel = label.split("\\.")[0].toLowerCase();
            if (commandMap.getCommand(cmdLabel) == null) {
                PluginCommand cmd = pluginConstructor.newInstance(cmdLabel, command.getPlugin());

                cmd.setExecutor(this);
                cmd.setTabCompleter(this);

                if(!commandMap.register(cmdLabel, cmd)) {
                    plugin.getLogger().severe("Unable to add the command " + cmdLabel);
                    return;
                }
            }
            if (!command.getDescription().equalsIgnoreCase("") && cmdLabel.equals(label)) {
                Objects.requireNonNull(commandMap.getCommand(cmdLabel)).setDescription(command.getDescription());
            }
            if (!command.getUsage().equalsIgnoreCase("") && cmdLabel.equals(label)) {
                Objects.requireNonNull(commandMap.getCommand(cmdLabel)).setUsage(command.getUsage());
            }

            String[] labelParts = label.split("\\.");
            int labelSize = labelParts.length;
            StringBuilder currentLabel = new StringBuilder();
            for (int i = 0; i < labelParts.length - 1; i++) {
                if (i > 0) {
                    currentLabel.append(".");
                }
                currentLabel.append(labelParts[i]);

                String completionPart = labelParts[i + 1];

                this.registerCompletion(currentLabel.toString(), i + 1, () -> Lists.newArrayList(completionPart));
            }

            for (int i = 0; i < args.size(); i++) {
                Argument arg = args.get(i);
                String[] parts = arg.arg().split(":");
                String type = parts[1].trim();
                ArgumentConverter<?> converter = this.typeConverters.get(type).getValue();
                if (arg.completion() != null) {
                    this.registerCompletion(label,labelSize + i, arg::completion);
                } else if (converter instanceof TabConverter tabConverter) {
                    this.registerCompletion(label,labelSize + i, tabConverter);
                } else {
                    this.registerCompletion(label, labelSize + i, ArrayList::new);
                }
            }
            for (int i = 0; i < optArgs.size(); i++) {
                Argument arg = optArgs.get(i);
                String[] parts = arg.arg().split(":");
                String type = parts[1].trim();
                ArgumentConverter<?> converter = this.typeConverters.get(type).getValue();
                if (arg.completion() != null) {
                    this.registerCompletion(label,labelSize + args.size() + i, arg::completion);
                } else if (converter instanceof TabConverter tabConverter) {
                    this.registerCompletion(label,labelSize + args.size() + i, tabConverter);
                } else {
                    this.registerCompletion(label, labelSize + args.size() + i, ArrayList::new);
                }
            }

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register a tab completer in the command manager.
     * @param label The label of the command.
     * @param commandSize The size of the command.
     * @param converter The converter of the tab completer.
     */
    private void registerCompletion(String label, int commandSize, TabConverter converter) {
        Map<Integer, TabConverter> mapInner = this.completers.getOrDefault(label, new HashMap<>());
        TabConverter newConverter;
        TabConverter converterInner = mapInner.getOrDefault(commandSize, null);
        if(converterInner != null) {
            newConverter = () -> {
                List<String> completions = new ArrayList<>(converterInner.onCompletion());
                completions.addAll(converter.onCompletion());
                return completions;
            };
        } else {
            newConverter = converter;
        }
        mapInner.put(commandSize, newConverter);
        this.completers.put(label, mapInner);
    }


    private boolean checkTypeForArgs(ArrayList<Argument> args) throws TypeArgumentNotExistException {
        for(String arg: args.stream().map(Argument::arg).toList()) {
            String[] parts = arg.split(":");

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
     * Parse the arguments of the command.
     * @param command The command to parse.
     * @param args The arguments to parse.
     * @return The arguments parsed.
     * @throws TypeArgumentNotExistException If the type of the argument does not exist.
     * @throws ArgumentIncorrectException If the argument is incorrect.
     */
    private Arguments parse(Command<?> command, String[] args) throws TypeArgumentNotExistException, ArgumentIncorrectException {
        Arguments arguments = new Arguments();
        ArrayList<Argument> templates = command.getArgs();
        for (int i = 0; i < templates.size(); i++) {
            String input = args[i];
            if (applyParsing(args, arguments, templates, i, input)) break;
        }

        ArrayList<Argument> optArgs = command.getOptinalArgs();
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
     * @param i The index of the argument.
     * @param input The input of the argument.
     * @return  If the parsing is applied.
     * @throws TypeArgumentNotExistException If the type of the argument does not exist.
     * @throws ArgumentIncorrectException If the argument is incorrect.
     */
    private boolean applyParsing(String[] args, Arguments arguments, ArrayList<Argument> templates, int i,
                                 String input) throws TypeArgumentNotExistException, ArgumentIncorrectException {
        String template = templates.get(i).arg();
        String[] parts = template.split(":");

        if (parts.length != 2) {
            throw new TypeArgumentNotExistException();
        }

        String key = parts[0].trim();
        String type = parts[1].trim();

        if (type.equals("infinite")) {
            StringBuilder builder = new StringBuilder();
            for (int ii = i; ii < args.length; ii++) {
                builder.append(args[ii]);
                if (ii < args.length - 1) {
                    builder.append(" ");
                }
            }
            arguments.add(key, String.class, builder.toString());
            return true;
        }

        if (typeConverters.containsKey(type)) {
            Map.Entry<Class<?>, ArgumentConverter<?>> converterWithType = typeConverters.get(type);
            Class<?> typeClass = converterWithType.getKey();
            ArgumentConverter<?> converter = converterWithType.getValue();
            Object obj = converter.apply(input);
            if (obj == null) {
                throw new ArgumentIncorrectException(input);
            }
            arguments.add(key, typeClass, obj);
        }
        return false;
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
     * This method is called when a command is executed.
     * @param sender The sender of the command.
     * @param command The command executed.
     * @param label The label of the command.
     * @param args The arguments of the command.
     * @return If the command is executed.
     */
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!plugin.isEnabled()) {
            return false;
        }

        if (!command.testPermission(sender)) {
            return true;
        }

        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());
            for (int x = 0; x < i; x++) {
                buffer.append(".").append(args[x].toLowerCase());
            }
            String cmdLabel = buffer.toString();
            if (commands.containsKey(cmdLabel)) {
                Command<?> commandFramework = commands.get(cmdLabel);
                if (!commandFramework.getPermission().isEmpty() && !sender.hasPermission(commandFramework.getPermission())) {
                    sender.sendMessage(Lang.translate(Messages.NO_PERMISSION));
                    return true;
                }
                if (commandFramework.inGameOnly() && !(sender instanceof Player)) {
                    sender.sendMessage(Lang.translate(Messages.ONLY_IN_GAME));
                    return true;
                }
                int subCommand = cmdLabel.split("\\.").length - 1;
                String[] modArgs = new String[args.length - subCommand];
                if (args.length - subCommand >= 0)
                    System.arraycopy(args, subCommand, modArgs, 0, args.length - subCommand);

                if (modArgs.length < commandFramework.getArgs().size()) {
                    String usage = command.getUsage();
                    if (usage.isEmpty()) {
                        usage = Lang.translate(Messages.MISSING_ARGS);
                    }
                    sender.sendMessage("§c" +usage);
                    return true;
                }

                if (!commandFramework.isInfiniteArgs() && (modArgs.length > commandFramework.getArgs().size() + commandFramework.getOptinalArgs().size())) {
                    String usage = command.getUsage();
                    if (usage.isEmpty()) {
                        usage = Lang.translate(Messages.MISSING_ARGS);
                    }
                    sender.sendMessage("§c" + usage);
                    return true;
                }

                Arguments arguments;
                try {
                    arguments = this.parse(commandFramework, modArgs);
                } catch (TypeArgumentNotExistException e) {
                    throw new RuntimeException(e);
                } catch (ArgumentIncorrectException e) {
                    String message = Lang.translate(Messages.ARG_NOT_RECOGNIZED);
                    message = message.replace("%arg%", e.getInput());
                    sender.sendMessage( message);
                    return true;
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
                    List<String> completer = converter.onCompletion().stream().filter(str -> str.toLowerCase().startsWith(arg.toLowerCase()) || str.equalsIgnoreCase(arg)).toList();
                    return completer.stream().filter(str -> {
                        String cmdLabelInner = cmdLabel + "." + str.toLowerCase();
                        if(this.commands.containsKey(cmdLabelInner)) {
                            Command<?> frameworkCommand = this.commands.get(cmdLabelInner);
                            return frameworkCommand.getPermission().isEmpty() || commandSender.hasPermission(frameworkCommand.getPermission());
                        }
                        return true;
                    }).collect(Collectors.toList());
                }
            }
        }

        return List.of();
    }

    /**
     * Get the commands of the command manager.
     * @return The commands of the command manager.
     */
    public Map<String, Command<?>> getCommands() {
        return commands;
    }
}
