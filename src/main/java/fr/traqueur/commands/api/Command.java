package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.arguments.TabConverter;
import fr.traqueur.commands.api.exceptions.ArgsWithInfiniteArgumentException;
import fr.traqueur.commands.api.requirements.Requirement;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is the base class for all commands.
 * It contains all the necessary methods to create a command.
 * It is abstract and must be inherited to be used.
 * @param <T> The plugin that owns the command.
 */
public abstract class Command<T extends JavaPlugin> {

    private CommandManager manager;
    // Attributs de la classe
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
    private final List<Command<?>> subcommands;

    /**
     * The arguments of the command.
     */
    private final List<Argument> args;

    /**
     * The optional arguments of the command.
     */
    private final List<Argument> optionalArgs;

    /**
     * The requirements of the command.
     */
    private final List<Requirement> requirements;

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
    protected void setManager(CommandManager manager) {
        this.manager = manager;
    }

    /**
     * This method is called when the command is executed.
     * @param sender The sender of the command.
     * @param args The arguments of the command.
     */
    public abstract void execute(CommandSender sender, Arguments args);

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
    protected final String getName() {
        return name;
    }

    /**
     * This method is called to get the description of the command.
     * @return The description of the command.
     */
    protected final String getDescription() {
        return description;
    }

    /**
     * This method is called to get the permission of the command.
     * @return The permission of the command.
     */
    protected final String getPermission() {
        return permission;
    }

    /**
     * This method is called to get the usage of the command.
     * @return The usage of the command.
     */
    protected final String getUsage() {
        return usage;
    }

    /**
     * This method is called to get the aliases of the command.
     * @return The aliases of the command.
     */
    protected final List<String> getAliases() {
        return aliases;
    }


    /**
     * This method is called to get the subcommands of the command.
     * @return The subcommands of the command.
     */
    protected final List<Command<?>> getSubcommands() {
        return subcommands;
    }

    /**
     * This method is called to get the arguments of the command.
     * @return The arguments of the command.
     */
    protected final List<Argument> getArgs() {
        return args;
    }

    /**
     * This method is called to get the optional arguments of the command.
     * @return The optional arguments of the command.
     */
    protected final List<Argument> getOptinalArgs() {
        return optionalArgs;
    }

    /**
     * This method is called to check if the command is only to use in game.
     * @return If the command is only to use in game.
     */
    protected final boolean inGameOnly() {
        return gameOnly;
    }

    /**
     * This method is called to get the requirements of the command.
     * @return The requirements of the command.
     */
    protected final List<Requirement> getRequirements() {
        return requirements;
    }

    /**
     * This method is called to check if the command has infinite arguments.
     * @return If the command has infinite arguments.
     */
    protected final boolean isInfiniteArgs() {
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
    public final void addSubCommand(Command<?>... commands) {
        List<Command<?>> commandsList = Arrays.asList(commands);
        commandsList.forEach(command -> command.setSubcommand(true));
        this.subcommands.addAll(commandsList);
    }

    /**
     * This method is called to add arguments to the command.
     * @param args The arguments to add.
     */
    public final void addArgs(String... args) {
        Arrays.asList(args).forEach(arg -> this.addArgs(arg, null));
    }

    /**
     * This method is called to add arguments to the command.
     * @param arg The argument to add.
     * @param converter The converter of the argument.
     */
    public final void addArgs(String arg, TabConverter converter) {
        try {
            if (this.infiniteArgs) {
                throw new ArgsWithInfiniteArgumentException(false);
            }

            if (arg.contains(":infinite")) {
                this.infiniteArgs = true;
            }
            this.args.add(new Argument(arg, converter));
        } catch (ArgsWithInfiniteArgumentException e) {
            this.plugin.getLogger().severe(e.getMessage());
        }
    }

    /**
     * This method is called to add optional arguments to the command.
     * @param args The optional arguments to add.
     */
    public final void addOptinalArgs(String... args) {
        Arrays.asList(args).forEach(arg -> this.addOptinalArgs(arg, null));
    }

    /**
     * This method is called to add optional arguments to the command.
     * @param arg The optional argument to add.
     * @param converter The converter of the argument.
     */
    public final void addOptinalArgs(String arg, TabConverter converter) {
        try {
            if (this.infiniteArgs) {
                throw new ArgsWithInfiniteArgumentException(true);
            }
            this.optionalArgs.add(new Argument(arg, converter));
        } catch (ArgsWithInfiniteArgumentException e) {
            this.plugin.getLogger().severe(e.getMessage());
        }
    }

    /**
     * This method is called to add requirements to the command.
     * @param requirement The requirements to add.
     */
    public final void addRequirements(Requirement... requirement) {
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
     * Set if the command is subcommand
     * @param subcommand the new value
     */
    public final void setSubcommand(boolean subcommand) {
        this.subcommand = subcommand;
    }

    /**
     * This method is called to get the plugin that owns the command.
     * @return The plugin that owns the command.
     */
    public final T getPlugin() {
        return plugin;
    }
}
