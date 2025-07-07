package fr.traqueur.commands.api.models.collections;

import fr.traqueur.commands.api.models.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CommandTreeTest {
    private CommandTree<String, String> tree;
    private StubCommand rootCmd;
    private StubCommand subCmd;
    private StubCommand subSubCmd;

    @BeforeEach
    void setup() {
        tree = new CommandTree<>();
        rootCmd = new StubCommand("root");
        subCmd = new StubCommand("sub");
        subSubCmd = new StubCommand("subsub");
    }

    @Test
    void testAddAndFindRoot() {
        tree.addCommand("root",rootCmd);
        // find base with no args
        Optional<CommandTree.MatchResult<String,String>> match = tree.findNode("root", new String[]{});
        assertTrue(match.isPresent());
        assertEquals(rootCmd, match.get().node.getCommand().orElse(null));
        // full label
        assertEquals("root", match.get().node.getFullLabel());
    }

    @Test
    void testAddNestedAndFind() {
        // create root.sub command hierarchy
        rootCmd.addSubCommand(subCmd);
        subCmd.addSubCommand(subSubCmd);
        tree.addCommand("root",rootCmd);
        tree.addCommand("root.sub",subCmd);
        tree.addCommand("root.sub.subsub",subSubCmd);

        // find sub
        Optional<CommandTree.MatchResult<String,String>> m1 = tree.findNode("root", new String[]{"sub"});
        assertTrue(m1.isPresent());
        assertEquals(subCmd, m1.get().node.getCommand().orElse(null));
        assertArrayEquals(new String[]{}, m1.get().args);

        // find sub.subsub
        Optional<CommandTree.MatchResult<String,String>> m2 = tree.findNode("root", new String[]{"sub", "subsub"});
        assertTrue(m2.isPresent());
        assertEquals(subSubCmd, m2.get().node.getCommand().orElse(null));
        assertArrayEquals(new String[]{}, m2.get().args);
    }

    @Test
    void testFindNodeWithExtraArgs() {
        tree.addCommand("root",rootCmd);
        // root takes no args, so extra args are leftover
        Optional<CommandTree.MatchResult<String,String>> m = tree.findNode("root", new String[]{"a","b","c"});
        assertTrue(m.isPresent());
        assertEquals(rootCmd, m.get().node.getCommand().orElse(null));
        assertArrayEquals(new String[]{"a","b","c"}, m.get().args);
    }

    @Test
    void testFindNonexistent() {
        tree.addCommand("root",rootCmd);
        Optional<CommandTree.MatchResult<String,String>> m = tree.findNode("unknown", new String[]{});
        assertFalse(m.isPresent());
    }

    @Test
    void testRemoveCommandClearOnly() {
        tree.addCommand("root",rootCmd);
        // remove without subcommands flag false, but no children => pruned
        tree.removeCommand("root", false);
        Optional<CommandTree.MatchResult<String,String>> m = tree.findNode("root", new String[]{});
        assertFalse(m.isPresent());
    }

    @Test
    void testRemoveCommandKeepChildren() {
        // add root and sub
        rootCmd.addSubCommand(subCmd);
        tree.addCommand("root", rootCmd);
        tree.addCommand("root.sub",subCmd);

        // remove root only, keep children
        tree.removeCommand("root", false);
        // root command cleared but sub-tree remains
        Optional<CommandTree.MatchResult<String,String>> mSub = tree.findNode("root", new String[]{"sub"});
        assertTrue(mSub.isPresent());
        assertEquals(subCmd, mSub.get().node.getCommand().orElse(null));

        // find root itself => cleared, so no command at root
        Optional<CommandTree.MatchResult<String,String>> mRoot = tree.findNode("root", new String[]{});
        assertFalse(mRoot.get().node.getCommand().isPresent());
    }

    @Test
    void testRemoveCommandPruneBranch() {
        // add nested commands
        rootCmd.addSubCommand(subCmd);
        subCmd.addSubCommand(subSubCmd);
        tree.addCommand("root",rootCmd);
        tree.addCommand("root.sub",subCmd);
        tree.addCommand("root.sub.subsub",subSubCmd);

        // remove entire branch
        tree.removeCommand("root.sub", true);
        // sub and subsub removed
        assertFalse(tree.findNode("root", new String[]{"sub"}).isPresent());
        assertFalse(tree.findNode("root", new String[]{"sub","subsub"}).isPresent());
        // root remains
        assertTrue(tree.findNode("root", new String[]{}).isPresent());
    }

    // stub Command to use in tests
    static class StubCommand extends Command<String, String> {
        public StubCommand(String name) { super(null, name); }
        @Override public void execute(String sender, fr.traqueur.commands.api.arguments.Arguments args) {}
    }
}
