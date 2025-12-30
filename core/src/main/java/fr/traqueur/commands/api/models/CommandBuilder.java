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

    public CommandBuilder<T, S> description(String description) {
        this.description = description;
        return this;
    }

    public CommandBuilder<T, S> usage(String usage) {
        this.usage = usage;
        return this;
    }

    public CommandBuilder<T, S> permission(String permission) {
        this.permission = permission;
        return this;
    }

    public CommandBuilder<T, S> gameOnly() {
        this.gameOnly = true;
        return this;
    }

    public CommandBuilder<T, S> gameOnly(boolean gameOnly) {
        this.gameOnly = gameOnly;
        return this;
    }

    public CommandBuilder<T, S> alias(String alias) {
        this.command.addAlias(alias);
        return this;
    }

    public CommandBuilder<T, S> aliases(String... aliases) {
        this.command.addAlias(aliases);
        return this;
    }

    public CommandBuilder<T, S> arg(String name, Class<?> type) {
        this.command.addArg(name, type);
        return this;
    }

    public CommandBuilder<T, S> arg(String name, Class<?> type, TabCompleter<S> completer) {
        this.command.addArg(name, type, completer);
        return this;
    }

    public CommandBuilder<T, S> optionalArg(String name, Class<?> type) {
        this.command.addOptionalArg(name, type);
        return this;
    }

    public CommandBuilder<T, S> optionalArg(String name, Class<?> type, TabCompleter<S> completer) {
        this.command.addOptionalArg(name, type, completer);
        return this;
    }

    public CommandBuilder<T, S> requirement(Requirement<S> requirement) {
        this.command.addRequirements(requirement);
        return this;
    }

    @SafeVarargs
    public final CommandBuilder<T, S> requirements(Requirement<S>... requirements) {
        this.command.addRequirements(requirements);
        return this;
    }

    public CommandBuilder<T, S> subcommand(Command<T, S> subcommand) {
        this.command.addSubCommand(subcommand);
        return this;
    }

    @SafeVarargs
    public final CommandBuilder<T, S> subcommands(Command<T, S>... subcommands) {
        this.command.addSubCommand(subcommands);
        return this;
    }

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