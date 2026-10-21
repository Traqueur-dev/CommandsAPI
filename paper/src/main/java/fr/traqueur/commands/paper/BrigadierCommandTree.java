package fr.traqueur.commands.paper;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.api.utils.Patterns;
import fr.traqueur.commands.impl.arguments.BooleanArgument;
import fr.traqueur.commands.impl.arguments.NumberArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Turns the commands of a manager into Brigadier nodes.
 * <p>
 * Each label becomes a literal, each typed argument a Brigadier argument, so that the client
 * knows the shape of the command: it colours the arguments, refuses a value of the wrong type
 * before sending, and suggests the values of the tab completers.
 * </p>
 * <ul>
 *     <li>{@code int}/{@code Integer}, {@code long}, {@code double}, {@code float}, {@code byte}
 *     become the matching Brigadier number, {@code boolean} becomes a Brigadier boolean, as long as
 *     their converter is the one of the library;</li>
 *     <li>an infinite argument becomes a greedy string, up to the end of the line;</li>
 *     <li>every other type, {@code String} and the types with a custom converter, becomes a word,
 *     with the suggestions of its tab completer.</li>
 * </ul>
 * <p>
 * The permission and the requirements of a command become the {@code requires} of its literal:
 * a sender who cannot run a command does not see it. The check reads the manager every time,
 * so a command unregistered after the nodes were built is hidden as well.
 * </p>
 * <p>
 * Running a node hands the line to the manager's {@link fr.traqueur.commands.api.models.CommandInvoker},
 * so conversion, checks and messages are the same as on every other platform.
 * </p>
 *
 * @param <T> The type of the plugin.
 * @param <S> The type of the sender.
 * @param <C> The type of the Brigadier source.
 * @since 5.6.0
 */
public final class BrigadierCommandTree<T, S, C> {

    private final CommandManager<T, S> manager;
    private final Function<C, S> senderOf;

    /**
     * Create a builder for the commands of a manager.
     *
     * @param manager  The manager holding the commands.
     * @param senderOf Finds the sender behind a Brigadier source.
     */
    public BrigadierCommandTree(CommandManager<T, S> manager, Function<C, S> senderOf) {
        this.manager = manager;
        this.senderOf = senderOf;
    }

    /**
     * The root labels of the manager, each of which {@link #build(String)} turns into a literal.
     *
     * @return The root labels, aliases included.
     */
    public List<String> roots() {
        return new ArrayList<>(this.manager.getCommands().getRoot().getChildren().keySet());
    }

    /**
     * Build the Brigadier node of a root label.
     *
     * @param label The root label.
     * @return The node, or null when the manager has no such label.
     */
    public LiteralCommandNode<C> build(String label) {
        CommandTree.CommandNode<T, S> node = this.manager.getCommands().getRoot().getChildren()
                .get(label.toLowerCase(Locale.ROOT));
        return node == null ? null : this.literal(node).build();
    }

    private LiteralArgumentBuilder<C> literal(CommandTree.CommandNode<T, S> node) {
        String fullLabel = node.getFullLabel();
        LiteralArgumentBuilder<C> literal = LiteralArgumentBuilder.literal(node.getLabel());
        literal.requires(source -> this.visible(this.senderOf.apply(source), fullLabel));

        Optional<Command<T, S>> command = node.getCommand();
        command.ifPresent(cmd -> this.arguments(literal, cmd, fullLabel));

        for (CommandTree.CommandNode<T, S> child : node.getChildren().values()) {
            literal.then(this.literal(child));
        }
        return literal;
    }

    /**
     * Chain the arguments of a command under its literal. A node is executable once every required
     * argument is given: the client then flags an incomplete command before it is sent.
     */
    private void arguments(LiteralArgumentBuilder<C> literal, Command<T, S> command, String fullLabel) {
        List<Argument<S>> required = command.getArgs();
        List<Argument<S>> all = new ArrayList<>(required);
        all.addAll(command.getOptionalArgs());

        if (required.isEmpty()) {
            literal.executes(this::execute);
        }
        if (all.isEmpty()) {
            return;
        }

        int depth = Patterns.DOT.split(fullLabel).length;
        List<RequiredArgumentBuilder<C, ?>> builders = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            Argument<S> argument = all.get(i);
            RequiredArgumentBuilder<C, ?> builder = RequiredArgumentBuilder.argument(argument.name(), this.typeOf(argument));
            TabCompleter<S> completer = this.completerOf(fullLabel, depth + i, argument);
            if (completer != null) {
                builder.suggests((context, suggestions) -> this.suggest(context, suggestions, completer, depth));
            }
            if (i >= required.size() - 1) {
                builder.executes(this::execute);
            }
            builders.add(builder);
            if (argument.isInfinite()) {
                break;
            }
        }

        ArgumentBuilder<C, ?> next = builders.get(builders.size() - 1);
        for (int i = builders.size() - 2; i >= 0; i--) {
            next = builders.get(i).then(next);
        }
        literal.then(next);
    }

    /**
     * The Brigadier type of an argument. Numbers and booleans are typed only when the library's own
     * converter reads them: a plugin that registered its own converter for {@code Integer} (to read
     * {@code 1k}, say) keeps a word, which its converter then parses.
     */
    ArgumentType<?> typeOf(Argument<S> argument) {
        if (argument.isInfinite()) {
            return StringArgumentType.greedyString();
        }
        Class<?> type = argument.type().key();
        ArgumentConverter<?> converter = this.manager.getConverter(type);
        if (converter instanceof NumberArgument<?>) {
            if (type == int.class || type == Integer.class) {
                return IntegerArgumentType.integer();
            }
            if (type == long.class || type == Long.class) {
                return LongArgumentType.longArg();
            }
            if (type == double.class || type == Double.class) {
                return DoubleArgumentType.doubleArg();
            }
            if (type == float.class || type == Float.class) {
                return FloatArgumentType.floatArg();
            }
            if (type == byte.class || type == Byte.class) {
                return IntegerArgumentType.integer(Byte.MIN_VALUE, Byte.MAX_VALUE);
            }
        }
        if (converter instanceof BooleanArgument<?> && (type == boolean.class || type == Boolean.class)) {
            return BoolArgumentType.bool();
        }
        return StringArgumentType.word();
    }

    /**
     * The tab completer of an argument: the one the manager computed for its position, which already
     * merges the argument's own completer and its type's. Typed arguments keep Brigadier's own
     * suggestions unless the argument brings a completer of its own.
     */
    private TabCompleter<S> completerOf(String fullLabel, int position, Argument<S> argument) {
        boolean typed = !(this.typeOf(argument) instanceof StringArgumentType);
        if (typed && argument.tabCompleter() == null) {
            return null;
        }
        Map<Integer, TabCompleter<S>> completers = this.manager.getCompleters().get(fullLabel);
        return completers == null ? null : completers.get(position);
    }

    private CompletableFuture<Suggestions> suggest(CommandContext<C> context, SuggestionsBuilder builder,
                                                   TabCompleter<S> completer, int depth) {
        String line = builder.getInput().substring(this.start(context));
        String[] tokens = line.split(" ", -1);
        List<String> args = Arrays.asList(Arrays.copyOfRange(tokens, Math.min(depth, tokens.length), tokens.length));
        String current = builder.getRemaining().toLowerCase(Locale.ROOT);

        List<String> completions = completer.onCompletion(this.senderOf.apply(context.getSource()), args);
        if (completions != null) {
            for (String completion : completions) {
                if (completion != null && completion.toLowerCase(Locale.ROOT).startsWith(current)) {
                    builder.suggest(completion);
                }
            }
        }
        return builder.buildFuture();
    }

    private int execute(CommandContext<C> context) {
        String line = context.getInput().substring(this.start(context), context.getRange().getEnd());
        String[] parts = line.split(" ");
        String base = parts[0];
        int namespace = base.indexOf(':');
        if (namespace >= 0) {
            base = base.substring(namespace + 1);
        }
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);
        this.manager.getInvoker().invoke(this.senderOf.apply(context.getSource()), base, args);
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }

    /**
     * Where the command starts in the input, which is not 0 when it runs through a redirect such as
     * {@code /execute as @p run ...}.
     */
    private int start(CommandContext<C> context) {
        return context.getNodes().isEmpty() ? context.getRange().getStart() : context.getNodes().get(0).getRange().getStart();
    }

    /**
     * Whether a sender sees a label: it must still be registered, and the sender must have the
     * permission and meet the requirements of its command. A label without a command of its own is
     * visible when one of its subcommands is.
     */
    private boolean visible(S sender, String fullLabel) {
        Optional<CommandTree.CommandNode<T, S>> node = this.manager.getCommands()
                .findNode(Patterns.DOT.split(fullLabel))
                .map(CommandTree.MatchResult::node);
        return node.isPresent() && this.visible(sender, node.get());
    }

    private boolean visible(S sender, CommandTree.CommandNode<T, S> node) {
        Optional<Command<T, S>> command = node.getCommand();
        if (command.isEmpty()) {
            return node.getChildren().values().stream().anyMatch(child -> this.visible(sender, child));
        }
        Command<T, S> cmd = command.get();
        String permission = cmd.getPermission();
        if (!permission.isEmpty() && !this.manager.getPlatform().hasPermission(sender, permission)) {
            return false;
        }
        return cmd.getRequirements().stream().allMatch(requirement -> requirement.check(sender));
    }
}
