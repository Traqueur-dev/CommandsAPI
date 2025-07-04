package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.exceptions.ArgsWithInfiniteArgumentException;
import fr.traqueur.commands.api.requirements.Requirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is the base class for all commands.
 * It contains all the necessary methods to create a command.
 * It is abstract and must be inherited to be used.
 * @param <T> The plugin that owns the command.
 */
public abstract class Command<T, S> {

    private CommandManager<T, S> manager;

    /**
     * The plugin that owns the command.
     */
    private final T plugin;

    /**
     * The name of the command.
     */
    private final String name;

    /**
     * The aliases of the command.
     */
    private final List<String> aliases;

    /**
     * The subcommands of the command.
     */
    private final List<Command<T, S>> subcommands;

    /**
     * The arguments of the command.
     */
    private final List<Argument<S>> args;

    /**
     * The optional arguments of the command.
     */
    private final List<Argument<S>> optionalArgs;

    /**
     * The requirements of the command.
     */
    private final List<Requirement<S>> requirements;

    /**
     * The description of the command.
     */
    private String description;

    /**
     * The usage of the command.
     */
    private String usage;

    /**
     * The permission of the command.
     */
    private String permission;

    /**
     * If the command is only for the game.
     */
    private boolean gameOnly;

    /**
     * If the command has infinite arguments.
     */
    private boolean infiniteArgs;

    /**
     * If the command is subcommand
     */
    private boolean subcommand;

    /**
     * The constructor of the command.
     * @param plugin The plugin that owns the command.
     * @param name The name of the command.
     */
    public Command(T plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.permission = "";
        this.usage = "";
        this.description = "";
        this.gameOnly = false;
        this.aliases = new ArrayList<>();
        this.subcommands = new ArrayList<>();
        this.args = new ArrayList<>();
        this.optionalArgs = new ArrayList<>();
        this.requirements = new ArrayList<>();
        this.subcommand = false;
    }

    /**
     * This method is called to set the manager of the command.
     * @param manager The manager of the command.
     */
    protected void setManager(CommandManager<T, S> manager) {
        this.manager = manager;
    }

    /**
     * This method is called when the command is executed.
     * @param context The context of the command execution.
     */
    public abstract void execute(CommandContext<S> context);

    /**
     * This method is called to unregister the command.
     */
    public void unregister() {
        this.unregister(true);
    }

    /**
     * This method is called to unregister the command.
     * @param subcommands If the subcommands must be unregistered.
     */
    public void unregister(boolean subcommands) {
        if(this.manager == null) {
            throw new IllegalArgumentException("The command is not registered.");
        }
        this.manager.unregisterCommand(this, subcommands);
    }

    /**
     * This method is called to get the name of the command.
     * @return The name of the command.
     */
    public final String getName() {
        return name;
    }

    /**
     * This method is called to get the description of the command.
     * @return The description of the command.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * This method is called to get the permission of the command.
     * @return The permission of the command.
     */
    public final String getPermission() {
        return permission;
    }

    /**
     * This method is called to get the usage of the command.
     * @return The usage of the command.
     */
    public final String getUsage() {
        return usage;
    }

    /**
     * This method is called to get the aliases of the command.
     * @return The aliases of the command.
     */
    public final List<String> getAliases() {
        return aliases;
    }


    /**
     * This method is called to get the subcommands of the command.
     * @return The subcommands of the command.
     */
    public final List<Command<T, S>> getSubcommands() {
        return subcommands;
    }

    /**
     * This method is called to get the arguments of the command.
     * @return The arguments of the command.
     */
    public final List<Argument<S>> getArgs() {
        return args;
    }

    /**
     * This method is called to get the optional arguments of the command.
     * @return The optional arguments of the command.
     */
    public final List<Argument<S>> getOptinalArgs() {
        return optionalArgs;
    }

    /**
     * This method is called to check if the command is only to use in game.
     * @return If the command is only to use in game.
     */
    public final boolean inGameOnly() {
        return gameOnly;
    }

    /**
     * This method is called to get the requirements of the command.
     * @return The requirements of the command.
     */
    public final List<Requirement<S>> getRequirements() {
        return requirements;
    }

    /**
     * This method is called to check if the command has infinite arguments.
     * @return If the command has infinite arguments.
     */
    public final boolean isInfiniteArgs() {
        return infiniteArgs;
    }

    /**
     * This method is called to set the description of the command
     * @param description The description of the command.
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * This method is called to set if the command is only to use in game.
     * @param gameOnly If the command is only to use in game.
     */
    public final void setGameOnly(boolean gameOnly) {
        this.gameOnly = gameOnly;
    }

    /**
     * This method is called to set the permission of the command.
     * @param permission The permission of the command.
     */
    public final void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * This method is called to set the usage of the command.
     * @param usage The usage of the command.
     */
    public final void setUsage(String usage) {
        this.usage = usage;
    }

    /**
     * This method is called to add aliases to the command.
     * @param aliases The aliases to add.
     */
    public final void addAlias(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
    }

    /**
     * This method is called to add a alias to the command.
     * @param alias The alias to add.
     */
    public final void addAlias(String alias) {
        this.aliases.add(alias);
    }

    /**
     * This method is called to add subcommands to the command.
     * @param commands The subcommands to add.
     */
    @SafeVarargs
    public final void addSubCommand(Command<T, S>... commands) {
        List<Command<T, S>> commandsList = Arrays.asList(commands);
        commandsList.forEach(Command::setSubcommand);
        this.subcommands.addAll(commandsList);
    }

    /**
     * This method is called to add arguments to the command.
     * @param args The arguments to add.
     */
    public final void addArgs(Object... args) {
        if (Arrays.stream(args).allMatch(arg -> arg instanceof String)) {
            for (Object arg : args) {
                String argStr = (String) arg;
                this.addArgs(argStr);
            }
            return;
        }

        if (args.length % 2 != 0 && !(args[1] instanceof String)) {
            throw new IllegalArgumentException("You must provide a type for the argument.");
        }

        for (int i = 0; i < args.length; i += 2) {
            if(!(args[i] instanceof String && args[i + 1] instanceof Class<?>)) {
                throw new IllegalArgumentException("You must provide a type for the argument.");
            }
            this.addArgs((String) args[i], (Class<?>) args[i + 1]);
        }
    }

    public final void addArgs(String arg) {
        if(!arg.contains(CommandManager.TYPE_PARSER)) {
            this.addArgs(arg, String.class, null);
        } else {
            this.addArgs(arg, null, null);
        }
    }

    public final void addArgs(String arg, Class<?> type) {
        this.addArgs(arg, type,null);
    }

    public final void addArgs(String arg, TabCompleter<S> converter) {
        if(!arg.contains(CommandManager.TYPE_PARSER)) {
            this.addArgs(arg, String.class, converter);
        } else {
            this.addArgs(arg, null, converter);
        }
    }

    /**
     * This method is called to add arguments to the command.
     * @param arg The argument to add.
     * @param converter The converter of the argument.
     */
    public final void addArgs(String arg, Class<?> type, TabCompleter<S> converter) {
        if (arg.contains(CommandManager.TYPE_PARSER) && type != null) {
            throw new IllegalArgumentException("You can't use the type parser in the command arguments.");
        }
        if(type == null && !arg.contains(CommandManager.TYPE_PARSER)) {
            throw new IllegalArgumentException("You must provide a type for the argument.");
        }

        if(type != null) {
            arg = arg + CommandManager.TYPE_PARSER + type.getSimpleName().toLowerCase();
        }

        this.add(arg, converter, false);
    }

    /**
     * This method is called to add arguments to the command.
     * @param args The arguments to add.
     */
    public final void addOptionalArgs(Object... args) {
        if (Arrays.stream(args).allMatch(arg -> arg instanceof String)) {
            for (Object arg : args) {
                String argStr = (String) arg;
                this.addOptionalArgs(argStr);
            }
            return;
        }

        if (args.length % 2 != 0 && !(args[1] instanceof String)) {
            throw new IllegalArgumentException("You must provide a type for the argument.");
        }

        for (int i = 0; i < args.length; i += 2) {
            if(!(args[i] instanceof String && args[i + 1] instanceof Class<?>)) {
                throw new IllegalArgumentException("You must provide a type for the argument.");
            }
            this.addOptionalArgs((String) args[i], (Class<?>) args[i + 1]);
        }
    }

    public final void addOptionalArgs(String arg) {
        if (!arg.contains(CommandManager.TYPE_PARSER)) {
            this.addOptionalArgs(arg, String.class, null);
            return;
        }
        this.addOptionalArgs(arg, null, null);
    }

    public final void addOptionalArgs(String arg, Class<?> type) {
        this.addOptionalArgs(arg, type,null);
    }

    public final void addOptionalArgs(String arg, TabCompleter<S> converter) {
        if (!arg.contains(CommandManager.TYPE_PARSER)) {
            this.addOptionalArgs(arg, String.class, converter);
            return;
        }
        this.addOptionalArgs(arg, null, converter);
    }

    /**
     * This method is called to add arguments to the command.
     * @param arg The argument to add.
     * @param converter The converter of the argument.
     */
    public final void addOptionalArgs(String arg, Class<?> type, TabCompleter<S> converter) {
        if (arg.contains(CommandManager.TYPE_PARSER) && type != null) {
            throw new IllegalArgumentException("You can't use the type parser in the command arguments.");
        }
        if(type != null) {
            arg = arg + CommandManager.TYPE_PARSER + type.getSimpleName().toLowerCase();
        }

        this.add(arg, converter, true);
    }

    private void add(String arg, TabCompleter<S> converter, boolean opt) {
        try {
            if (this.infiniteArgs) {
                throw new ArgsWithInfiniteArgumentException(false);
            }

            if (arg.contains(":infinite")) {
                this.infiniteArgs = true;
            }
            if(opt) {
                this.optionalArgs.add(new Argument<>(arg, converter));
            } else {
                this.args.add(new Argument<>(arg, converter));
            }
        } catch (ArgsWithInfiniteArgumentException e) {
            this.manager.getLogger().error(e.getMessage());
        }
    }

    /**
     * This method is called to add requirements to the command.
     * @param requirement The requirements to add.
     */
    public final void addRequirements(Requirement<S>... requirement) {
        requirements.addAll(Arrays.asList(requirement));
    }

    /**
     * Check if the command is subcommand
     * @return if the command is subcommand
     */
    public final boolean isSubCommand() {
        return subcommand;
    }

    /**
     * This method is called to get the plugin that owns the command.
     * @return The plugin that owns the command.
     */
    public final T getPlugin() {
        return plugin;
    }

    /**
     * This method is called to generate a default usage for the command.
     * @return The default usage of the command.
     */
    public String generateDefaultUsage(CommandPlatform<T,S> platform, S sender, String label) {
        StringBuilder usage = new StringBuilder();
        usage.append("/");
        Arrays.stream(label.split("\\.")).forEach(s -> usage.append(s).append(" "));

        StringBuilder firstArg = new StringBuilder();
        this.getSubcommands()
                .stream().filter(subCommand -> subCommand.getPermission().isEmpty() || platform.hasPermission(sender, subCommand.getPermission()))
                .forEach(subCommand -> firstArg.append(subCommand.getName()).append("|"));
        if(firstArg.length() > 0) {
            firstArg.deleteCharAt(firstArg.length() - 1);
            usage.append("<").append(firstArg).append(">");
        }
        if((!this.getArgs().isEmpty() || !this.getOptinalArgs().isEmpty()) && firstArg.length() > 0) {
            usage.append("|");
        }

        usage.append(this.getArgs().stream().map(argument -> "<" + argument.arg() + ">").collect(Collectors.joining(" ")));
        usage.append(" ");
        usage.append(this.getOptinalArgs().stream().map(argument -> "[" + argument.arg() + "]").collect(Collectors.joining(" ")));
        return usage.toString();
    }

    /**
     * Set if the command is subcommand
     */
    private void setSubcommand() {
        this.subcommand = true;
    }
}
