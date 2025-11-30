package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.CommandRegistrationException;
import fr.traqueur.commands.api.exceptions.TypeArgumentNotExistException;
import fr.traqueur.commands.api.logging.Logger;
import fr.traqueur.commands.api.logging.MessageHandler;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandInvoker;
import fr.traqueur.commands.api.models.CommandPlatform;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.api.updater.Updater;
import fr.traqueur.commands.impl.arguments.BooleanArgument;
import fr.traqueur.commands.impl.arguments.DoubleArgument;
import fr.traqueur.commands.impl.arguments.IntegerArgument;
import fr.traqueur.commands.impl.arguments.LongArgument;
import fr.traqueur.commands.impl.logging.InternalLogger;
import fr.traqueur.commands.impl.logging.InternalMessageHandler;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is the command manager.
 * It allows you to register commands and subcommands.
 * It also allows you to register argument converters and tab completer.
 * @param <T> The type of the platform that will use this command manager.
 * @param <S> The type of the sender that will use this command manager.
 */
public abstract class CommandManager<T, S> {

    /**
     * The parser used to separate the type of the argument from its name.
     * <p> For example: "player:Player" will be parsed as "player" and "Player". </p>
     */
    public static final String TYPE_PARSER = ":";

    /**
     * The parser used to separate the infinite argument from its name.
     * <p> For example: "args:infinite" will be parsed as "args" and "infinite". </p>
     */
    private static final String INFINITE = "infinite";

    private final CommandPlatform<T,S> platform;

    /**
     * The commands registered in the command manager.
     */
    private final CommandTree<T,S> commands;

    /**
     * The argument converters registered in the command manager.
     */
    private final Map<String, Map.Entry<Class<?>, ArgumentConverter<?>>> typeConverters;

    /**
     * The tab completer registered in the command manager.
     */
    private final Map<String, Map<Integer, TabCompleter<S>>> completers;


    private final CommandInvoker<T,S> invoker;

    /**
     * The message handler of the command manager.
     */
    private MessageHandler messageHandler;

    /**
     * The logger of the command manager.
     */
    private Logger logger;

    /**
     * If the debug mode is enabled.
     */
    private boolean debug;


    /**
     * Create a new command manager.
     * @param platform The platform of the command manager.
     */
    public CommandManager(CommandPlatform<T,S> platform) {
        Updater.checkUpdates();
        this.platform = platform;
        this.platform.injectManager(this);
        this.messageHandler = new InternalMessageHandler();
        this.logger = new InternalLogger(platform.getLogger());
        this.debug = false;
        this.commands = new CommandTree<>();
        this.typeConverters = new HashMap<>();
        this.completers = new HashMap<>();
        this.invoker = new CommandInvoker<>(this);
        this.registerInternalConverters();
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
        this.messageHandler = messageHandler;
    }

    /**
     * Get the message handler of the command manager.
     * @return The message handler of the command manager.
     */
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    /**
     * Set the debug mode of the command manager.
     * @param debug If the debug mode is enabled.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Get the debug mode of the command manager.
     * @return If the debug mode is enabled.
     */
    public boolean isDebug() {
        return this.debug;
    }

    /**
     * Register a command in the command manager.
     * @param command The command to register.
     */
    public void registerCommand(Command<T,S> command) {
        try {
            for (String alias : command.getAliases()) {
                this.addCommand(command, alias);
                this.registerSubCommands(alias, command.getSubcommands());
            }
        } catch(TypeArgumentNotExistException e) {
            throw new CommandRegistrationException("Failed to register command: " + command.getClass().getSimpleName(), e);
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
        String[] rawArgs = label.split("\\.");
        Optional<Command<T,S>> commandOptional = this.commands.findNode(rawArgs)
                .flatMap(result -> result.node.getCommand());

        if (!commandOptional.isPresent()) {
            throw new IllegalArgumentException("Command with label '" + label + "' does not exist.");
        }
        this.unregisterCommand(commandOptional.get(), subcommands);
    }

    /**
     * Unregister a command in the command manager.
     * @param command The command to unregister.
     */
    public void unregisterCommand(Command<T,S> command) {
        this.unregisterCommand(command, true);
    }

    /**
     * Unregister a command in the command manager.
     * @param command The command to unregister.
     * @param subcommands If the subcommands must be unregistered.
     */
    public void unregisterCommand(Command<T,S> command, boolean subcommands) {
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
     * @param converter The converter of the argument.
     * @param <C> The type of the argument.
     */
    public <C> void registerConverter(Class<C> typeClass, ArgumentConverter<C> converter) {
        this.typeConverters.put(typeClass.getSimpleName().toLowerCase(), new AbstractMap.SimpleEntry<>(typeClass, converter));
    }

    /**
     * Register an argument converter in the command manager.
     * @param typeClass The class of the type.
     * @param type The type of the argument.
     * @param converter The converter of the argument.
     * @param <C> The type of the argument.
     */
    @Deprecated
    public <C> void registerConverter(Class<C> typeClass, String type,  ArgumentConverter<C> converter) {
        this.typeConverters.put(type, new AbstractMap.SimpleEntry<>(typeClass, converter));
    }

    /**
     * Parse the arguments of the command.
     * @param command The command to parse.
     * @param args The arguments to parse.
     * @return The arguments parsed.
     * @throws TypeArgumentNotExistException If the type of the argument does not exist.
     * @throws ArgumentIncorrectException If the argument is incorrect.
     */
    public Arguments parse(Command<T,S> command, String[] args) throws TypeArgumentNotExistException, ArgumentIncorrectException {
        Arguments arguments = new Arguments(this.logger);
        List<Argument<S>> templates = command.getArgs();
        for (int i = 0; i < templates.size(); i++) {
            String input = args[i];
            if (applyParsing(args, arguments, templates, i, input)) break;
        }

        List<Argument<S>> optArgs = command.getOptinalArgs();
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
     * Get the commands of the command manager.
     * @return The commands of the command manager.
     */
    public CommandTree<T, S> getCommands() {
        return commands;
    }


    /**
     * Get the completers of the command manager
     * @return The completers of command manager
     */
    public Map<String, Map<Integer, TabCompleter<S>>> getCompleters() {
        return this.completers;
    }

    /**
     * Get the platform of the command manager.
     * @return The platform of the command manager.
     */
    public CommandPlatform<T,S> getPlatform() {
        return platform;
    }

    /**
     * Get the logger of the command manager.
     * @return The logger of the command manager.
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Register a list of subcommands in the command manager.
     * @param parentLabel The parent label of the commands.
     * @param subcommands The list of subcommands to register.
     * @throws TypeArgumentNotExistException If the type of the argument does not exist.
     */
    private void registerSubCommands(String parentLabel, List<Command<T,S>> subcommands) throws TypeArgumentNotExistException {
        if(subcommands == null || subcommands.isEmpty()) {
            return;
        }
        for (Command<T,S> subcommand : subcommands) {
            // getAliases() already returns [name, ...aliases], so no need to add the name again
            List<String> aliasesSub = new ArrayList<>(subcommand.getAliases());
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
    private void unregisterSubCommands(String parentLabel, List<Command<T,S>> subcommandsList) {
        if(subcommandsList == null || subcommandsList.isEmpty()) {
            return;
        }
        for (Command<T,S> subcommand : subcommandsList) {
            // getAliases() already returns [name, ...aliases], so no need to add the name again
            List<String> aliasesSub = new ArrayList<>(subcommand.getAliases());
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
        this.platform.removeCommand(label, subcommand);
        this.commands.removeCommand(label, subcommand);
        this.completers.remove(label);
    }

    /**
     * Register a command in the command manager.
     * @param command The command to register.
     * @param label The label of the command.
     * @throws TypeArgumentNotExistException If the type of the argument does not exist.
     */
    private void addCommand(Command<T,S> command, String label) throws TypeArgumentNotExistException {
        if(this.isDebug()) {
            this.logger.info("Register command " + label);
        }
        List<Argument<S>> args = command.getArgs();
        List<Argument<S>> optArgs = command.getOptinalArgs();
        String[] labelParts = label.split("\\.");
        int labelSize = labelParts.length;

        if(!this.checkTypeForArgs(args) || !this.checkTypeForArgs(optArgs)) {
            throw new TypeArgumentNotExistException();
        }

        command.setManager(this);
        this.platform.addCommand(command, label);
        commands.addCommand(label, command);

        this.addCompletionsForLabel(labelParts);
        this.addCompletionForArgs(label, labelSize, args);
        this.addCompletionForArgs(label, labelSize + args.size(), optArgs);
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
            this.addCompletion(currentLabel.toString(), i + 1, (s, args) -> new ArrayList<>(Collections.singleton(completionPart)));
        }
    }

    /**
     * Register the completions of the arguments.
     * @param label The label of the command.
     * @param commandSize The size of the command.
     * @param args The arguments to register.
     */
    private void addCompletionForArgs(String label, int commandSize, List<Argument<S>> args) {
        for (int i = 0; i < args.size(); i++) {
            Argument<S> arg = args.get(i);
            String[] parts = arg.arg().split(TYPE_PARSER);
            String type = parts[1].trim();
            ArgumentConverter<?> converter = this.typeConverters.get(type).getValue();
            TabCompleter<S> argConverter = arg.tabConverter();
            if (argConverter != null) {
                this.addCompletion(label,commandSize + i, argConverter);
            } else if (converter instanceof TabCompleter) {
                @SuppressWarnings("unchecked")
                TabCompleter<S> tabCompleter = (TabCompleter<S>) converter;
                this.addCompletion(label,commandSize + i, tabCompleter);
            } else {
                this.addCompletion(label, commandSize + i, (s, argsInner) -> new ArrayList<>());
            }
        }
    }

    /**
     * Register a tab completer in the command manager.
     * @param label The label of the command.
     * @param commandSize The size of the command.
     * @param converter The converter of the tab completer.
     */
    private void addCompletion(String label, int commandSize, TabCompleter<S> converter) {
        Map<Integer, TabCompleter<S>> mapInner = this.completers.getOrDefault(label, new HashMap<>());

        TabCompleter<S> combined;
        TabCompleter<S> existing = mapInner.get(commandSize);

        if (existing != null) {
            combined = (s,args) -> {
                List<String> completions = new ArrayList<>(existing.onCompletion(s,args));
                completions.addAll(converter.onCompletion(s,args));
                return completions;
            };
        } else {
            combined = converter;
        }

        mapInner.put(commandSize, combined);
        this.completers.put(label, mapInner);
    }

    /**
     * Check if the type of the argument exists.
     * @param args The arguments to check.
     */
    private boolean checkTypeForArgs(List<Argument<S>> args) throws TypeArgumentNotExistException {
        for(String arg: args.stream().map(Argument::arg).collect(Collectors.toList())) {
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
    private boolean applyParsing(String[] args, Arguments arguments, List<Argument<S>> templates, int argIndex,
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
     * Get the command invoker of the command manager.
     * @return The command invoker of the command manager.
     */
    public CommandInvoker<T, S> getInvoker() {
        return invoker;
    }

    /**
     * Register the internal converters of the command manager.
     */
    private void registerInternalConverters() {
        this.registerConverter(String.class,  (s) -> s);
        this.registerConverter(Boolean.class, new BooleanArgument<>());
        this.registerConverter(Integer.class, new IntegerArgument());
        this.registerConverter(Double.class, new DoubleArgument());
        this.registerConverter(Long.class,  new LongArgument());
        this.registerConverter(String.class, INFINITE, s -> s);
    }
}
