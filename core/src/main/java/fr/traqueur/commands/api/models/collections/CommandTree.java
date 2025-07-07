package fr.traqueur.commands.api.models.collections;

import fr.traqueur.commands.api.models.Command;

import java.util.*;

/**
 * A prefix-tree of commands, supporting nested labels and argument fallback.
 */
public class CommandTree<T, S> {
    /**
     * Result of a lookup: the deepest matching node and leftover args.
     */
    public static class MatchResult<T, S> {
        public final CommandNode<T, S> node;
        public final String[] args;
        public MatchResult(CommandNode<T, S> node, String[] args) {
            this.node = node;
            this.args = args;
        }
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

        /** segment without parent prefix */
        public String getLabel() {
            return label;
        }

        /** full path joined by dots */
        public String getFullLabel() {
            if (parent == null || parent.label == null) return label;
            return parent.getFullLabel() + "." + label;
        }

        /** optional command at this node */
        public Optional<Command<T, S>> getCommand() {
            return Optional.ofNullable(command);
        }

        /** immutable view of children */
        public Map<String, CommandNode<T, S>> getChildren() {
            return Collections.unmodifiableMap(children);
        }
    }

    private final CommandNode<T, S> root = new CommandNode<>(null, null);

    /**
     * Add or replace a command at the given full label path (dot-separated).
     * @param label full path like "hello.sub"
     * @param command the command to attach at that path
     */
    public void addCommand(String label, Command<T, S> command) {
        String[] parts = label.split("\\.");
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
            } else if (node.hadChildren) {
                // expected a subcommand but not found
                return Optional.empty();
            } else {
                break;
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
     * @param label full path like "root.sub"
     * @param prune if true, remove entire subtree; otherwise just clear the command at that node
     */
    public void removeCommand(String label, boolean prune) {
        String[] parts = label.split("\\.");
        CommandNode<T, S> node = root;
        for (String seg : parts) {
            node = node.children.get(seg.toLowerCase());
            if (node == null) return;
        }
        CommandNode<T, S> parent = node.parent;
        if (parent == null) return; // cannot remove root

        boolean hasChildren = !node.children.isEmpty();
        if (prune || !hasChildren) {
            // remove this node and entire subtree
            parent.children.remove(node.label);
        } else {
            // clear only the command, keep subtree intact
            node.command = null;
        }
    }

    /** Access to the virtual root. */
    public CommandNode<T, S> getRoot() {
        return root;
    }
}
