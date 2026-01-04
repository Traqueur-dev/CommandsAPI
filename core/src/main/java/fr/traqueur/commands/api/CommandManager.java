package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.TypeArgumentNotExistException;
import fr.traqueur.commands.api.logging.Logger;
import fr.traqueur.commands.api.logging.MessageHandler;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandBuilder;
import fr.traqueur.commands.api.models.CommandInvoker;
import fr.traqueur.commands.api.models.CommandPlatform;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.api.parsing.ArgumentParser;
import fr.traqueur.commands.api.parsing.ParseError;
import fr.traqueur.commands.api.parsing.ParseResult;
import fr.traqueur.commands.api.updater.Updater;
import fr.traqueur.commands.impl.arguments.BooleanArgument;
import fr.traqueur.commands.impl.arguments.DoubleArgument;
import fr.traqueur.commands.impl.arguments.IntegerArgument;
import fr.traqueur.commands.impl.arguments.LongArgument;
import fr.traqueur.commands.impl.logging.InternalLogger;
import fr.traqueur.commands.impl.logging.InternalMessageHandler;
import fr.traqueur.commands.impl.parsing.DefaultArgumentParser;

import java.util.*;

/**
 * This class is the command manager.
 * It allows you to register commands and subcommands.
 * It also allows you to register argument converters and tab completer.
 *
 * @param <T> The type of the platform that will use this command manager.
 * @param <S> The type of the sender that will use this command manager.
 */
public abstract class CommandManager<T, S> {


    private final ArgumentParser<T, S, String[]> parser;
    private final CommandPlatform<T, S> platform;

    /**
     * The commands registered in the command manager.
     */
    private final CommandTree<T, S> commands;

    /**
     * The argument converters registered in the command manager.
     */
    private final Map<Class<?>, ArgumentConverter.Wrapper<?>> typeConverters;

    /**
     * The tab completer registered in the command manager.
     */
    private final Map<String, Map<Integer, TabCompleter<S>>> completers;


    private final CommandInvoker<T, S> invoker;

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
     *
     * @param platform The platform of the command manager.
     */
    public CommandManager(CommandPlatform<T, S> platform) {
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
        this.parser = new DefaultArgumentParser<>(this.typeConverters, this.logger);
        this.registerInternalConverters();
    }

    /**
     * Get the message handler of the command manager.
     *
     * @return The message handler of the command manager.
     */
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    /**
     * Set the message handler of the command manager.
     *
     * @param messageHandler The message handler to set.
     */
    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * Get the debug mode of the command manager.
     *
     * @return If the debug mode is enabled.
     */
    public boolean isDebug() {
        return this.debug;
    }

    /**
     * Set the debug mode of the command manager.
     *
     * @param debug If the debug mode is enabled.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Register a command in the command manager.
     *
     * @param command The command to register.
     */
    public void registerCommand(Command<T, S> command) {
        for (String label : command.getAllLabels()) {
            this.addCommand(command, label);
            this.registerSubCommands(label, command.getSubcommands());
        }
    }

    /**
     * Unregister a command in the command manager.
     *
     * @param label The label of the command to unregister.
     */
    public void unregisterCommand(String label) {
        this.unregisterCommand(label, true);
    }

    /**
     * Unregister a command in the command manager.
     *
     * @param label       The label of the command to unregister.
     * @param subcommands If the subcommands must be unregistered.
     */
    public void unregisterCommand(String label, boolean subcommands) {
        String[] rawArgs = label.split("\\.");
        Optional<Command<T, S>> commandOptional = this.commands.findNode(rawArgs)
                .flatMap(result -> result.node().getCommand());

        if (commandOptional.isEmpty()) {
            throw new IllegalArgumentException("Command with label '" + label + "' does not exist.");
        }
        this.unregisterCommand(commandOptional.get(), subcommands);
    }

    /**
     * Unregister a command in the command manager.
     *
     * @param command The command to unregister.
     */
    public void unregisterCommand(Command<T, S> command) {
        this.unregisterCommand(command, true);
    }

    /**
     * Unregister a command in the command manager.
     *
     * @param command     The command to unregister.
     * @param subcommands If the subcommands must be unregistered.
     */
    public void unregisterCommand(Command<T, S> command, boolean subcommands) {
        List<String> labels = new ArrayList<>(command.getAllLabels());
        for (String label : labels) {
            this.removeCommand(label, subcommands);
            if (subcommands) {
                this.unregisterSubCommands(label, command.getSubcommands());
            }
        }
    }

    /**
     * Register an argument converter in the command manager.
     *
     * @param typeClass The class of the type.
     * @param converter The converter of the argument.
     * @param <C>       The type of the argument.
     */
    public <C> void registerConverter(Class<C> typeClass, ArgumentConverter<C> converter) {
        this.typeConverters.put(typeClass, new ArgumentConverter.Wrapper<>(typeClass, converter));
    }

    /**
     * Parse the arguments of the command.
     *
     * @param command The command to parse.
     * @param args    The arguments to parse.
     * @return The arguments parsed.
     * @throws TypeArgumentNotExistException If the type of the argument does not exist.
     * @throws ArgumentIncorrectException    If the argument is incorrect.
     */
    public Arguments parse(Command<T, S> command, String[] args) throws TypeArgumentNotExistException, ArgumentIncorrectException {
        ParseResult result = parser.parse(command, args);
        if (!result.isSuccess()) {
            ParseError error = result.error();
            switch (error.type()) {
                case TYPE_NOT_FOUND -> throw new TypeArgumentNotExistException();
                case CONVERSION_FAILED -> throw new ArgumentIncorrectException(error.input());
                default -> throw new ArgumentIncorrectException(error.message());
            }
        }
        return result.arguments();
    }

    /**
     * Get the commands of the command manager.
     *
     * @return The commands of the command manager.
     */
    public CommandTree<T, S> getCommands() {
        return commands;
    }

    /**
     * Get the completers of the command manager
     *
     * @return The completers of command manager
     */
    public Map<String, Map<Integer, TabCompleter<S>>> getCompleters() {
        return this.completers;
    }

    /**
     * Check if a TabCompleter exists for the given type.
     *
     * @param type The type to check.
     * @return true if a TabCompleter is registered for this type.
     */
    public boolean hasTabCompleterForType(Class<?> type) {
        ArgumentConverter.Wrapper<?> wrapper = this.typeConverters.get(type);
        return wrapper != null && wrapper.converter() instanceof TabCompleter;
    }

    /**
     * Get the TabCompleter for the given type.
     *
     * @param type The type to get the TabCompleter for.
     * @return The TabCompleter for this type, or null if none exists.
     */
    @SuppressWarnings("unchecked")
    public TabCompleter<S> getTabCompleterForType(Class<?> type) {
        ArgumentConverter.Wrapper<?> wrapper = this.typeConverters.get(type);
        if (wrapper != null && wrapper.converter() instanceof TabCompleter) {
            return (TabCompleter<S>) wrapper.converter();
        }
        return null;
    }

    /**
     * Get the platform of the command manager.
     *
     * @return The platform of the command manager.
     */
    public CommandPlatform<T, S> getPlatform() {
        return platform;
    }

    /**
     * Get the logger of the command manager.
     *
     * @return The logger of the command manager.
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Set the custom logger of the command manager.
     *
     * @param logger The logger to set.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Register a list of subcommands in the command manager.
     *
     * @param parentLabel The parent label of the commands.
     * @param subcommands The list of subcommands to register.
     */
    private void registerSubCommands(String parentLabel, List<Command<T, S>> subcommands) {
        if (subcommands == null || subcommands.isEmpty()) {
            return;
        }
        for (Command<T, S> subcommand : subcommands) {
            List<String> aliasesSub = new ArrayList<>(subcommand.getAllLabels());
            for (String aliasSub : aliasesSub) {
                this.addCommand(subcommand, parentLabel + "." + aliasSub);
                this.registerSubCommands(parentLabel + "." + aliasSub, subcommand.getSubcommands());
            }
        }
    }

    /**
     * Unregister the subcommands of a command.
     *
     * @param parentLabel     The parent label of the subcommands.
     * @param subcommandsList The list of subcommands to unregister.
     */
    private void unregisterSubCommands(String parentLabel, List<Command<T, S>> subcommandsList) {
        if (subcommandsList == null || subcommandsList.isEmpty()) {
            return;
        }
        for (Command<T, S> subcommand : subcommandsList) {
            List<String> labelsSub = subcommand.getAllLabels();
            for (String labelSub : labelsSub) {
                this.removeCommand(parentLabel + "." + labelSub, true);
                this.unregisterSubCommands(parentLabel + "." + labelSub, subcommand.getSubcommands());
            }
        }
    }

    /**
     * Unregister a command in the command manager.
     *
     * @param label      The label of the command.
     * @param subcommand If the subcommand must be unregistered.
     */
    private void removeCommand(String label, boolean subcommand) {
        this.platform.removeCommand(label, subcommand);
        this.commands.removeCommand(label, subcommand);
        this.completers.remove(label);
    }

    /**
     * Create a new command builder bound to this manager.
     * This allows for a fluent API without specifying generic types.
     *
     * @param name the command name
     * @return a new command builder
     */
    public CommandBuilder<T, S> command(String name) {
        return new CommandBuilder<>(this, name);
    }

    /**
     * Register a command in the command manager.
     *
     * @param command The command to register.
     * @param label   The label of the command.
     */
    private void addCommand(Command<T, S> command, String label) {
        if (this.isDebug()) {
            this.logger.info("Register command " + label);
        }
        List<Argument<S>> args = command.getArgs();
        List<Argument<S>> optArgs = command.getOptionalArgs();
        String[] labelParts = label.split("\\.");
        int labelSize = labelParts.length;

        command.setManager(this);
        this.platform.addCommand(command, label);
        commands.addCommand(label, command);

        this.addCompletionsForLabel(labelParts);
        this.addCompletionForArgs(label, labelSize, args);
        this.addCompletionForArgs(label, labelSize + args.size(), optArgs);
    }

    /**
     * Register the completions of the command.
     *
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
     *
     * @param label       The label of the command.
     * @param commandSize The size of the command.
     * @param args        The arguments to register.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addCompletionForArgs(String label, int commandSize, List<Argument<S>> args) {
        for (int i = 0; i < args.size(); i++) {
            Argument<S> arg = args.get(i);
            Class<?> type = arg.type().key();
            ArgumentConverter.Wrapper<?> entry = this.typeConverters.get(type);
            TabCompleter<S> argConverter = arg.tabCompleter();
            if (argConverter != null) {
                this.addCompletion(label, commandSize + i, argConverter);
            } else if (entry != null && entry.converter() instanceof TabCompleter completer) {
                this.addCompletion(label, commandSize + i, (TabCompleter<S>) completer);
            } else {
                this.addCompletion(label, commandSize + i, (s, argsInner) -> new ArrayList<>());
            }
        }
    }

    /**
     * Register a tab completer in the command manager.
     *
     * @param label       The label of the command.
     * @param commandSize The size of the command.
     * @param converter   The converter of the tab completer.
     */
    private void addCompletion(String label, int commandSize, TabCompleter<S> converter) {
        Map<Integer, TabCompleter<S>> mapInner = this.completers.getOrDefault(label, new HashMap<>());

        TabCompleter<S> combined;
        TabCompleter<S> existing = mapInner.get(commandSize);

        if (existing != null) {
            combined = (s, args) -> {
                List<String> completions = new ArrayList<>(existing.onCompletion(s, args));
                completions.addAll(converter.onCompletion(s, args));
                return completions;
            };
        } else {
            combined = converter;
        }

        mapInner.put(commandSize, combined);
        this.completers.put(label, mapInner);
    }

    /**
     * Get the command invoker of the command manager.
     *
     * @return The command invoker of the command manager.
     */
    public CommandInvoker<T, S> getInvoker() {
        return invoker;
    }

    /**
     * Register the internal converters of the command manager.
     */
    private void registerInternalConverters() {
        this.registerConverter(String.class, (s) -> s);

        // Register both primitive and wrapper types for DefaultArgumentParser (Spigot/Velocity).
        // JDA's ArgumentParser handles primitives internally, but text-based platforms need explicit registration.
        // Wrapper types (Integer.class, Long.class, etc.) are registered for compatibility with wrapper usage.
        // Primitive types (int.class, long.class, etc.) are registered to support primitive method parameters.
        this.registerConverter(Boolean.class, new BooleanArgument<>());
        this.registerConverter(boolean.class, new BooleanArgument<>());
        this.registerConverter(Integer.class, new IntegerArgument());
        this.registerConverter(int.class, new IntegerArgument());
        this.registerConverter(Double.class, new DoubleArgument());
        this.registerConverter(double.class, new DoubleArgument());
        this.registerConverter(Long.class, new LongArgument());
        this.registerConverter(long.class, new LongArgument());
    }
}
