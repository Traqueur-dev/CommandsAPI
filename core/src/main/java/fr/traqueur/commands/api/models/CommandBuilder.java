package fr.traqueur.commands.api.models;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.requirements.Requirement;

import java.util.function.BiConsumer;

/**
 * Fluent builder for creating commands without subclassing.
 *
 * @param <T> plugin type
 * @param <S> sender type
 */
public class CommandBuilder<T, S> {

    private final CommandManager<T, S> manager;
    private final SimpleCommand command;

    private String description = "";
    private String usage = "";
    private String permission = "";
    private boolean gameOnly = false;
    private BiConsumer<S, Arguments> executor;

    /**
     * Create a builder from a manager (preferred).
     *
     * @param manager the command manager
     * @param name    the command name
     */
    public CommandBuilder(CommandManager<T, S> manager, String name) {
        this.manager = manager;
        this.command = new SimpleCommand(manager.getPlatform().getPlugin(), name);
    }

    /**
     * Set the command description.
     *
     * @param description the description text
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Set the command usage string.
     *
     * @param usage the usage text
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> usage(String usage) {
        this.usage = usage;
        return this;
    }

    /**
     * Set the required permission for this command.
     *
     * @param permission the permission node
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> permission(String permission) {
        this.permission = permission;
        return this;
    }

    /**
     * Mark this command as game-only (cannot be used from console).
     *
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> gameOnly() {
        this.gameOnly = true;
        return this;
    }

    /**
     * Set whether this command is game-only.
     *
     * @param gameOnly true if game-only, false otherwise
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> gameOnly(boolean gameOnly) {
        this.gameOnly = gameOnly;
        return this;
    }

    /**
     * Add an alias for this command.
     *
     * @param alias the alias to add
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> alias(String alias) {
        this.command.addAlias(alias);
        return this;
    }

    /**
     * Add multiple aliases for this command.
     *
     * @param aliases the aliases to add
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> aliases(String... aliases) {
        this.command.addAlias(aliases);
        return this;
    }

    /**
     * Add a required argument to this command.
     *
     * @param name the argument name
     * @param type the argument type class
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> arg(String name, Class<?> type) {
        this.command.addArg(name, type);
        return this;
    }

    /**
     * Add a required argument with a custom tab completer.
     *
     * @param name      the argument name
     * @param type      the argument type class
     * @param completer the tab completer for this argument
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> arg(String name, Class<?> type, TabCompleter<S> completer) {
        this.command.addArg(name, type, completer);
        return this;
    }

    /**
     * Add an optional argument to this command.
     *
     * @param name the argument name
     * @param type the argument type class
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> optionalArg(String name, Class<?> type) {
        this.command.addOptionalArg(name, type);
        return this;
    }

    /**
     * Add an optional argument with a custom tab completer.
     *
     * @param name      the argument name
     * @param type      the argument type class
     * @param completer the tab completer for this argument
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> optionalArg(String name, Class<?> type, TabCompleter<S> completer) {
        this.command.addOptionalArg(name, type, completer);
        return this;
    }

    /**
     * Add a requirement that must be met to execute this command.
     *
     * @param requirement the requirement to add
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> requirement(Requirement<S> requirement) {
        this.command.addRequirements(requirement);
        return this;
    }

    /**
     * Add multiple requirements that must be met to execute this command.
     *
     * @param requirements the requirements to add
     * @return this builder for chaining
     */
    @SafeVarargs
    public final CommandBuilder<T, S> requirements(Requirement<S>... requirements) {
        this.command.addRequirements(requirements);
        return this;
    }

    /**
     * Add a subcommand to this command.
     *
     * @param subcommand the subcommand to add
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> subcommand(Command<T, S> subcommand) {
        this.command.addSubCommand(subcommand);
        return this;
    }

    /**
     * Add multiple subcommands to this command.
     *
     * @param subcommands the subcommands to add
     * @return this builder for chaining
     */
    @SafeVarargs
    public final CommandBuilder<T, S> subcommands(Command<T, S>... subcommands) {
        this.command.addSubCommand(subcommands);
        return this;
    }

    /**
     * Set the executor for this command.
     *
     * @param executor the executor that handles command execution
     * @return this builder for chaining
     */
    public CommandBuilder<T, S> executor(BiConsumer<S, Arguments> executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Build the command.
     *
     * @return the built command
     * @throws IllegalStateException if no executor is set
     */
    public Command<T, S> build() {
        if (this.executor == null) {
            throw new IllegalStateException("Command executor must be set");
        }

        this.command.setDescription(this.description);
        this.command.setUsage(this.usage);
        this.command.setPermission(this.permission);
        this.command.setGameOnly(this.gameOnly);
        this.command.setExecutor(this.executor);

        return this.command;
    }

    /**
     * Build and register the command.
     *
     * @return the built and registered command
     */
    public Command<T, S> register() {
        Command<T, S> cmd = build();
        this.manager.registerCommand(cmd);
        return cmd;
    }

    /**
     * Simple command implementation used internally by the builder.
     */
    private class SimpleCommand extends Command<T, S> {

        private BiConsumer<S, Arguments> executor;

        SimpleCommand(T plugin, String name) {
            super(plugin, name);
        }

        void setExecutor(BiConsumer<S, Arguments> executor) {
            this.executor = executor;
        }

        @Override
        public void execute(S sender, Arguments arguments) {
            if (executor != null) {
                executor.accept(sender, arguments);
            }
        }
    }
}