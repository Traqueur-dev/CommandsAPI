package fr.traqueur.commands.api.models.collections;

import fr.traqueur.commands.api.models.Command;

import java.util.*;

/**
 * A prefix-tree of commands, supporting nested labels and argument fallback.
 * @param <T> type of the command context
 * @param <S> type of the command sender
 */
public class CommandTree<T, S> {
    /**
     * Result of a lookup: the deepest matching node and leftover args.
     * This is used to find commands based on a base label and raw arguments.
     * @param <T> type of the command context
     * @param <S> type of the command sender
     */
    public static class MatchResult<T, S> {

        /** The node that matched the base label and any subcommands.
         * The args are the remaining segments after the match.
         */
        public final CommandNode<T, S> node;

        /** Remaining arguments after the matched node.
         * This can be empty if the match was exact.
         */
        public final String[] args;

        /** Create a match result with the node and leftover args.
         * @param node the matched command node
         * @param args remaining arguments after the match
         */
        public MatchResult(CommandNode<T, S> node, String[] args) {
            this.node = node;
            this.args = args;
        }
    }

    /**
     * A node representing one segment in the command path.
     * Each node can have a command associated with it,
     * @param <T> type of the command context
     * @param <S> type of the command sender
     */
    public static class CommandNode<T, S> {

        private final String label;
        private final CommandNode<T, S> parent;
        private final Map<String, CommandNode<T, S>> children = new HashMap<>();
        private Command<T, S> command;
        private boolean hadChildren = false;

        /** Create a new command node with the given label and optional parent.
         * @param label the segment label, e.g. "hello"
         * @param parent the parent node, or null for root
         */
        public CommandNode(String label, CommandNode<T, S> parent) {
            this.label = label;
            this.parent = parent;
        }

        /** Get the label of this node segment.
         * @return the label like "hello"
         */
        public String getLabel() {
            return label;
        }

        /**
         * Get the full label path including parent segments.
         * @return the full label like "parent.child"
         */
        public String getFullLabel() {
            if (parent == null || parent.label == null) return label;
            return parent.getFullLabel() + "." + label;
        }

        /** Get the command associated with this node, if any.
         * @return the command, or empty if not set
         */
        public Optional<Command<T, S>> getCommand() {
            return Optional.ofNullable(command);
        }

        /** Get the parent node, or null if this is the root.
         * @return the parent node, or null for root
         */
        public Map<String, CommandNode<T, S>> getChildren() {
            return Collections.unmodifiableMap(children);
        }
    }

    private final CommandNode<T, S> root;


    /**
     * Create an empty command tree with a root node.
     * The root node has no label and serves as the starting point for all commands.
     */
    public CommandTree() {
        this.root = new CommandNode<>(null, null);
    }

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
     * This allows for partial matches where the command may have subcommands.
     * @param base the base command label, e.g. "hello"
     * @param rawArgs the raw arguments to match against subcommands
     * @return an Optional containing the match result, or empty if not found
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
     * This finds the exact node matching all segments.
     * @param segments the path segments like ["root", "sub"]
     * @return an Optional containing the match result, or empty if not found
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
        CommandNode<T, S> target = this.findNode(label.split("\\.")).map(result -> result.node).orElse(null);
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
            if(parent.children.isEmpty()){
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
                if(parent.children.isEmpty()){
                    parent.hadChildren = false;
                }
            }
        }
    }

    /**
     * Get the root command node of this tree.
     * @return the root node, which has no label
     */
    public CommandNode<T, S> getRoot() {
        return root;
    }
}
