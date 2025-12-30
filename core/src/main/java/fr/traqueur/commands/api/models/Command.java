package fr.traqueur.commands.api.models;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.arguments.ArgumentType;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.requirements.Requirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is the base class for all commands.
 * It contains all the necessary methods to create a command.
 * It is abstract and must be inherited to be used.
 * @param <T> The plugin that owns the command.
 * @param <S> The type of the sender who use the command.
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

    private boolean enable;

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
        this.enable = true;
    }

    /**
     * This method is called to set the manager of the command.
     * @param manager The manager of the command.
     */
    public void setManager(CommandManager<T, S> manager) {
        this.manager = manager;
    }

    /**
     * This method is called when the command is executed.
     * @param sender The sender of the command.
     * @param arguments The arguments of the command.
     */
    public abstract void execute(S sender, Arguments arguments);

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
        return Collections.unmodifiableList(aliases);
    }

    public final List<String> getAllLabels() {
        List<String> labels = new ArrayList<>();
        labels.add(name);
        if (!this.aliases.isEmpty()) {
            labels.addAll(this.aliases);
        }
        return labels;
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
    public final List<Argument<S>> getOptionalArgs() {
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

        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("You must use the method like succession of String,Class<?>");
        }

        for (int i = 0; i < args.length; i += 2) {
            if (!(args[i] instanceof String argName && args[i + 1] instanceof Class<?> type)) {
                throw new IllegalArgumentException("You must provide a type for the argument.");
            }
            this.addArg(argName, type);
        }
    }

    /**
     * This method is called to add arguments to the command.
     * @param arg The argument to add.
     * @param type The type of the argument to add.
     */
    public final void addArg(String arg, Class<?> type) {
        this.addArg(arg, type, null);
    }

    /**
     * This method is called to add arguments to the command.
     * @param arg The argument to add.
     * @param converter The converter of the argument.
     * @param type The type of the argument, can be null if the argument is a string.
     */
    public final void addArg(String arg, Class<?> type, TabCompleter<S> converter) {
        this.add(arg, ArgumentType.of(type), converter, false);
    }

    /**
     * This method is called to add arguments to the command.
     * @param args The arguments to add.
     */
    public final void addOptionalArgs(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("You must provide a type for the argument.");
        }

        for (int i = 0; i < args.length; i += 2) {
            if (!(args[i] instanceof String argName && args[i + 1] instanceof Class<?> clazz)) {
                throw new IllegalArgumentException("You must provide a type for the argument.");
            }
            this.addOptionalArg(argName, clazz);
        }
    }

    /**
     * This method is called to add optional arguments to the command.
     * @param arg The argument to add.
     * @param type The type of the argument to add.
     */
    public final void addOptionalArg(String arg, Class<?> type) {
        this.addOptionalArg(arg, type, null);
    }

    /**
     * This method is called to add optional arguments to the command.
     * @param arg The argument to add.
     * @param converter The converter of the argument.
     */
    public final void addOptionalArg(String arg, Class<?> type, TabCompleter<S> converter) {
        this.add(arg, ArgumentType.of(type), converter, true);
    }

    private void add(String name, ArgumentType type, TabCompleter<S> completer, boolean optional) {
        if (this.infiniteArgs) {
            if (this.manager != null) {
                String msg = (optional ? "Optional arguments" : "Arguments") +
                        " cannot follow infinite arguments.";
                this.manager.getLogger().error(msg);
            }
        }

        if (type.isInfinite()) {
            this.infiniteArgs = true;
        }

        Argument<S> arg = new Argument<>(name, type, completer);

        if (optional) {
            this.optionalArgs.add(arg);
        } else {
            this.args.add(arg);
        }
    }

    /**
     * This method is called to add requirements to the command.
     * @param requirement The requirements to add.
     */
    @SafeVarargs
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
     * @param sender The sender of the command.
     * @param label The label of the command.
     * @return The default usage of the command.
     */
    public String generateDefaultUsage(S sender, String label) {
        StringBuilder usage = new StringBuilder("/");

        String[] parts = label.split("\\.");
        usage.append(String.join(" ", parts));

        List<Command<T, S>> directSubs = this.getSubcommands().stream()
                .filter(sub -> {
                    String perm = sub.getPermission();
                    return perm.isEmpty() || this.manager.getPlatform().hasPermission(sender, perm);
                })
                .toList();

        if (!directSubs.isEmpty()) {
            usage.append(" <");
            String subs = directSubs.stream()
                    .map(Command::getName)
                    .map(str -> str.split("\\.")[0])
                    .collect(Collectors.joining("|"));
            usage.append(subs).append(">");
        }

        if (!this.getArgs().isEmpty() || !this.getOptionalArgs().isEmpty()) {
            usage.append(!directSubs.isEmpty() ? "|" : " ");

            // arguments obligatoires : <name:type>
            String req = this.getArgs().stream()
                    .map(arg -> "<" + arg.canonicalName() + ">")
                    .collect(Collectors.joining(" "));
            usage.append(req);

            // arguments optionnels : [name:type]
            if (!this.getOptionalArgs().isEmpty()) {
                if (!req.isEmpty()) {
                    usage.append(" ");
                }
                String opt = this.getOptionalArgs().stream()
                        .map(arg -> "[" + arg.canonicalName() + "]")
                        .collect(Collectors.joining(" "));
                usage.append(opt);
            }
        }

        return usage.toString();
    }

    /**
     * Change the state of the command
     * @param state the new state for the command
     */
    public void setEnabled(boolean state) {
        this.enable = state;
    }

    /**
     * Check if the command is enabled
     * @return if the command is enabled
     */
    public boolean isEnabled() {
        return enable;
    }

    /**
     * Set if the command is subcommand
     */
    private void setSubcommand() {
        this.subcommand = true;
    }
}
