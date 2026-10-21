package fr.traqueur.commands.paper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.Infinite;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.requirements.Requirement;
import fr.traqueur.commands.test.mocks.MockCommandManager;
import fr.traqueur.commands.test.mocks.MockSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BrigadierCommandTreeTest {

    private MockCommandManager manager;
    private final List<Object> executed = new ArrayList<>();
    private final List<String> messages = new ArrayList<>();
    private Set<String> permissions;
    private MockSender sender;

    @BeforeEach
    void setUp() {
        manager = new MockCommandManager();
        permissions = new java.util.HashSet<>();
        sender = new MockSender() {
            @Override
            public void sendMessage(String message) {
                messages.add(message);
            }

            @Override
            public boolean hasPermission(String permission) {
                return permissions.contains(permission);
            }
        };
    }

    @Test
    void integerArgument_isRefusedBeforeItIsSent() {
        Command<Object, MockSender> give = command("give", args -> executed.add(args.<Integer>get("amount")));
        give.addArgs("amount", Integer.class);
        manager.registerCommand(give);
        CommandDispatcher<MockSender> dispatcher = dispatcher();

        CommandNode<MockSender> amount = dispatcher.getRoot().getChild("give").getChild("amount");
        assertInstanceOf(IntegerArgumentType.class, ((ArgumentCommandNode<?, ?>) amount).getType());

        ParseResults<MockSender> refused = dispatcher.parse("give abc", sender);
        assertTrue(!refused.getExceptions().isEmpty() || refused.getReader().canRead(),
                "the client parses the same tree: a non-number must not be accepted as the amount");
        assertThrows(CommandSyntaxException.class, () -> dispatcher.execute(refused));
        assertTrue(executed.isEmpty());

        assertDoesNotThrow(() -> dispatcher.execute("give 12", sender));
        assertEquals(List.of(12), executed);
    }

    @Test
    void missingRequiredArgument_isAnIncompleteCommand() {
        Command<Object, MockSender> give = command("give", args -> executed.add("ran"));
        give.addArgs("amount", Integer.class);
        manager.registerCommand(give);

        assertThrows(CommandSyntaxException.class, () -> dispatcher().execute("give", sender));
        assertTrue(executed.isEmpty());
    }

    @Test
    void optionalArguments_canBeLeftOut() throws CommandSyntaxException {
        Command<Object, MockSender> heal = command("heal", args -> executed.add(args.<Integer>getOptional("amount").orElse(-1)));
        heal.addOptionalArgs("amount", Integer.class);
        manager.registerCommand(heal);
        CommandDispatcher<MockSender> dispatcher = dispatcher();

        dispatcher.execute("heal", sender);
        dispatcher.execute("heal 4", sender);

        assertEquals(List.of(-1, 4), executed);
    }

    @Test
    void booleanAndWordAndGreedy_types() throws CommandSyntaxException {
        Command<Object, MockSender> say = command("say", args -> executed.add(args.get("loud") + "|" + args.get("target") + "|" + args.get("text")));
        say.addArgs("loud", Boolean.class, "target", String.class, "text", Infinite.class);
        manager.registerCommand(say);
        CommandDispatcher<MockSender> dispatcher = dispatcher();

        dispatcher.execute("say true bob hello the world", sender);

        assertEquals(List.of("true|bob|hello the world"), executed);
        assertThrows(CommandSyntaxException.class, () -> dispatcher.execute("say maybe bob hi", sender));
    }

    @Test
    void customConverter_isAWordWithTheTabCompleterSuggestions() throws Exception {
        manager.registerConverter(Warp.class, new WarpConverter());
        Command<Object, MockSender> warp = command("warp", args -> executed.add(args.<Warp>get("to").name()));
        warp.addArgs("to", Warp.class);
        manager.registerCommand(warp);
        CommandDispatcher<MockSender> dispatcher = dispatcher();

        CommandNode<MockSender> to = dispatcher.getRoot().getChild("warp").getChild("to");
        assertInstanceOf(StringArgumentType.class, ((ArgumentCommandNode<?, ?>) to).getType());
        List<String> all = suggestions(dispatcher, "warp ");
        List<String> filtered = suggestions(dispatcher, "warp s");

        assertEquals(Set.of("spawn", "shop", "nether"), Set.copyOf(all));
        assertEquals(Set.of("spawn", "shop"), Set.copyOf(filtered));

        dispatcher.execute("warp shop", sender);
        assertEquals(List.of("shop"), executed);
    }

    @Test
    void ownIntegerConverter_keepsAWord() {
        manager.registerConverter(Integer.class, s -> s.endsWith("k") ? Integer.parseInt(s.substring(0, s.length() - 1)) * 1000 : Integer.valueOf(s));
        Command<Object, MockSender> pay = command("pay", args -> executed.add(args.<Integer>get("amount")));
        pay.addArgs("amount", Integer.class);
        manager.registerCommand(pay);
        CommandDispatcher<MockSender> dispatcher = dispatcher();

        assertDoesNotThrow(() -> dispatcher.execute("pay 2k", sender));
        assertEquals(List.of(2000), executed);
    }

    @Test
    void permissionAndRequirements_hideTheCommand() {
        Command<Object, MockSender> admin = command("admin", args -> executed.add("admin"));
        admin.setPermission("valkn.admin");
        Command<Object, MockSender> nether = command("nether", args -> executed.add("nether"));
        boolean[] inNether = {false};
        nether.addRequirements(new Requirement<>() {
            @Override
            public boolean check(MockSender s) {
                return inNether[0];
            }

            @Override
            public String errorMessage() {
                return "";
            }
        });
        manager.registerCommand(admin);
        manager.registerCommand(nether);
        CommandDispatcher<MockSender> dispatcher = dispatcher();

        assertFalse(dispatcher.getRoot().getChild("admin").canUse(sender));
        assertFalse(dispatcher.getRoot().getChild("nether").canUse(sender));
        assertThrows(CommandSyntaxException.class, () -> dispatcher.execute("admin", sender));

        permissions.add("valkn.admin");
        inNether[0] = true;
        assertTrue(dispatcher.getRoot().getChild("admin").canUse(sender));
        assertTrue(dispatcher.getRoot().getChild("nether").canUse(sender));
    }

    @Test
    void subcommands_andAliases() throws CommandSyntaxException {
        Command<Object, MockSender> home = command("home", args -> executed.add("home"));
        home.addAlias("h");
        Command<Object, MockSender> set = command("set", args -> executed.add("set " + args.get("name")));
        set.addArgs("name", String.class);
        home.addSubCommand(set);
        manager.registerCommand(home);
        CommandDispatcher<MockSender> dispatcher = dispatcher();

        dispatcher.execute("home", sender);
        dispatcher.execute("home set base", sender);
        dispatcher.execute("h set mine", sender);

        assertEquals(List.of("home", "set base", "set mine"), executed);
    }

    @Test
    void unregisteredCommand_isHiddenAndNoLongerRuns() {
        Command<Object, MockSender> spawn = command("spawn", args -> executed.add("spawn"));
        manager.registerCommand(spawn);
        CommandDispatcher<MockSender> dispatcher = dispatcher();

        manager.unregisterCommand(spawn);

        assertFalse(dispatcher.getRoot().getChild("spawn").canUse(sender));
        assertThrows(CommandSyntaxException.class, () -> dispatcher.execute("spawn", sender));

        manager.registerCommand(spawn);
        assertDoesNotThrow(() -> dispatcher.execute("spawn", sender));
        assertEquals(List.of("spawn"), executed);
    }

    @Test
    void messagesStillComeFromTheInvoker() throws CommandSyntaxException {
        Command<Object, MockSender> off = command("off", args -> executed.add("off"));
        off.setEnabled(false);
        manager.registerCommand(off);

        dispatcher().execute("off", sender);

        assertEquals(List.of("&cThis command is currently disabled."), messages);
        assertTrue(executed.isEmpty());
    }

    private CommandDispatcher<MockSender> dispatcher() {
        BrigadierCommandTree<Object, MockSender, MockSender> tree = new BrigadierCommandTree<>(manager, source -> source);
        CommandDispatcher<MockSender> dispatcher = new CommandDispatcher<>();
        for (String label : tree.roots()) {
            dispatcher.getRoot().addChild(tree.build(label));
        }
        return dispatcher;
    }

    private List<String> suggestions(CommandDispatcher<MockSender> dispatcher, String input) throws Exception {
        return dispatcher.getCompletionSuggestions(dispatcher.parse(input, sender)).get()
                .getList().stream().map(Suggestion::getText).collect(Collectors.toList());
    }

    private static Command<Object, MockSender> command(String name, java.util.function.Consumer<Arguments> body) {
        return new Command<>(null, name) {
            @Override
            public void execute(MockSender s, Arguments arguments) {
                body.accept(arguments);
            }
        };
    }

    record Warp(String name) {
    }

    static class WarpConverter implements ArgumentConverter<Warp>, TabCompleter<MockSender> {
        @Override
        public Warp apply(String s) {
            return List.of("spawn", "shop", "nether").contains(s) ? new Warp(s) : null;
        }

        @Override
        public List<String> onCompletion(MockSender sender, List<String> args) {
            return List.of("spawn", "shop", "nether");
        }
    }
}
