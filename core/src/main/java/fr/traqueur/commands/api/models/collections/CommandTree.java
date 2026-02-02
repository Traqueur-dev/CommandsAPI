package fr.traqueur.commands.api.models.collections;

import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.utils.Patterns;

import java.util.*;
import java.util.regex.Pattern;

/**
 * A prefix-tree of commands, supporting nested labels and argument fallback.
 *
 * @param <T> type of the command context
 * @param <S> type of the command sender
 */
public class CommandTree<T, S> {

    /**
     * Valid label pattern: starts with letter, followed by letters, digits, underscores.
     * Each segment must start with a letter to ensure valid command syntax across platforms.
     * Example valid segments: "help", "setHome", "player_info"
     * Example invalid segments: "123cmd", "_hidden", "cmd-name"
     */
    private static final Pattern VALID_LABEL_SEGMENT = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

    /**
     * Maximum length for a single label segment (64 characters).
     * This limit prevents excessively long command names that could cause
     * display issues in help menus or tab completion interfaces.
     */
    private static final int MAX_SEGMENT_LENGTH = 64;

    /**
     * Maximum depth for nested commands (10 levels).
     * This limit prevents deeply nested command hierarchies that could
     * impact performance during command lookup and cause usability issues.
     * Example: "admin.user.permission.group.add" has depth 5.
     */
    private static final int MAX_DEPTH = 10;

    private CommandNode<T, S> root;

    public CommandTree() {
        this.root = new CommandNode<>(null, null);
    }

    public void clear() {
        this.root = new CommandNode<>(null, null);
    }

    /**
     * Add or replace a command at the given full label path (dot-separated).
     *
     * @param label   full path like "hello.sub"
     * @param command the command to attach at that path
     * @throws IllegalArgumentException if label is invalid
     */
    public void addCommand(String label, Command<T, S> command) {
        validateLabel(label);

        String[] parts = Patterns.DOT.split(label);
        CommandNode<T, S> node = root;

        for (String seg : parts) {
            String key = seg.toLowerCase();
            node.hadChildren = true;
            CommandNode<T, S> finalNode = node;
            node = node.children.computeIfAbsent(key, k -> new CommandNode<>(k, finalNode));
        }
        node.command = command;
    }

    /**
     * Validate a command label.
     *
     * @param label the label to validate
     * @throws IllegalArgumentException if invalid
     */
    private void validateLabel(String label) {
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException("Command label cannot be null or empty");
        }

        String[] segments = Patterns.DOT.split(label);

        if (segments.length > MAX_DEPTH) {
            throw new IllegalArgumentException(
                    "Command label exceeds max depth (" + MAX_DEPTH + "): " + label
            );
        }

        for (String segment : segments) {
            validateSegment(segment, label);
        }
    }

    /**
     * Validate a single segment of a label.
     *
     * @param segment   the segment to validate
     * @param fullLabel the full label for error messages
     * @throws IllegalArgumentException if invalid
     */
    private void validateSegment(String segment, String fullLabel) {
        if (segment.isEmpty()) {
            throw new IllegalArgumentException(
                    "Command label contains empty segment: " + fullLabel
            );
        }

        if (segment.length() > MAX_SEGMENT_LENGTH) {
            throw new IllegalArgumentException(
                    "Command label segment exceeds max length (" + MAX_SEGMENT_LENGTH + "): " + segment
            );
        }

        if (!VALID_LABEL_SEGMENT.matcher(segment).matches()) {
            throw new IllegalArgumentException(
                    "Invalid command label segment '" + segment + "' in: " + fullLabel +
                            ". Segments must start with a letter and contain only letters, digits, or underscores."
            );
        }
    }

    /**
     * Lookup a base label and raw arguments, returning matching node and leftover args.
     */
    public Optional<MatchResult<T, S>> findNode(String base, String[] rawArgs) {
        if (base == null) return Optional.empty();
        CommandNode<T, S> node = root.children.get(base.toLowerCase());
        if (node == null) return Optional.empty();

        int i = 0;
        while (i < rawArgs.length) {
            String seg = rawArgs[i].toLowerCase();
            CommandNode<T, S> child = node.children.get(seg);
            if (child != null) {
                node = child;
                i++;
            } else if (node.hadChildren && node.command == null) {
                return Optional.empty();
            } else if (node.command != null) {
                break;
            } else {
                return Optional.empty();
            }
        }
        String[] left = Arrays.copyOfRange(rawArgs, i, rawArgs.length);
        return Optional.of(new MatchResult<>(node, left));
    }

    /**
     * Lookup by full path segments, with no leftover args.
     */
    public Optional<MatchResult<T, S>> findNode(String[] segments) {
        if (segments == null || segments.length == 0) return Optional.empty();
        CommandNode<T, S> node = root;
        for (String seg : segments) {
            node = node.children.get(seg.toLowerCase());
            if (node == null) return Optional.empty();
        }
        return Optional.of(new MatchResult<>(node, new String[]{}));
    }

    /**
     * Remove a command node by its full label.
     */
    public void removeCommand(String label, boolean prune) {
        CommandNode<T, S> target = this.findNode(Patterns.DOT.split(label))
                .map(MatchResult::node)
                .orElse(null);
        if (target == null) return;

        if (prune) {
            pruneSubtree(target);
        } else {
            clearOrPruneEmpty(target);
        }
    }

    private void pruneSubtree(CommandNode<T, S> node) {
        CommandNode<T, S> parent = node.parent;
        if (parent != null) {
            parent.children.remove(node.label);
            if (parent.children.isEmpty()) {
                parent.hadChildren = false;
            }
        }
    }

    private void clearOrPruneEmpty(CommandNode<T, S> node) {
        node.command = null;
        if (node.children.isEmpty()) {
            CommandNode<T, S> parent = node.parent;
            if (parent != null) {
                parent.children.remove(node.label);
                if (parent.children.isEmpty()) {
                    parent.hadChildren = false;
                }
            }
        }
    }

    public CommandNode<T, S> getRoot() {
        return root;
    }

    /**
     * A node representing one segment in the command path.
     */
    public static class CommandNode<T, S> {

        private final String label;
        private final CommandNode<T, S> parent;
        private final Map<String, CommandNode<T, S>> children = new HashMap<>();
        private Command<T, S> command;
        private boolean hadChildren = false;

        public CommandNode(String label, CommandNode<T, S> parent) {
            this.label = label;
            this.parent = parent;
        }

        public String getLabel() {
            return label;
        }

        public String getFullLabel() {
            if (parent == null || parent.label == null) return label;
            return parent.getFullLabel() + "." + label;
        }

        public Optional<Command<T, S>> getCommand() {
            return Optional.ofNullable(command);
        }

        public Map<String, CommandNode<T, S>> getChildren() {
            return Collections.unmodifiableMap(children);
        }
    }

    /**
     * Result of a lookup: the deepest matching node and leftover args.
     */
    public record MatchResult<T, S>(CommandNode<T, S> node, String[] args) {
    }
}