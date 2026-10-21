package fr.traqueur.commands.adventure;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.logging.MessageKey;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.requirements.Requirement;
import fr.traqueur.commands.test.mocks.MockCommandManager;
import fr.traqueur.commands.test.mocks.MockSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComponentMessageFormatterTest {

    private MockCommandManager manager;
    private final List<Component> components = new ArrayList<>();
    private final List<String> texts = new ArrayList<>();
    private MockSender sender;

    @BeforeEach
    void setUp() {
        manager = new MockCommandManager();
        sender = new MockSender() {
            @Override
            public void sendMessage(String message) {
                texts.add(message);
            }

            @Override
            public boolean hasPermission(String permission) {
                return false;
            }
        };
    }

    @Test
    void clickAndHover_reachTheSenderIntact() {
        Map<MessageKey, String> templates = new EnumMap<>(MessageKey.class);
        templates.put(MessageKey.NO_PERMISSION,
                "<red>Interdit.</red> <click:run_command:'/spawn'><hover:show_text:'Retour au spawn'>Retour</hover></click>");
        install(ComponentMessageFormatter.miniMessage(templates::get));
        Command<Object, MockSender> command = command("admin");
        command.setPermission("admin.use");
        manager.registerCommand(command);

        manager.getInvoker().invoke(sender, "admin", new String[0]);

        assertTrue(texts.isEmpty(), "the message must not go through the text path");
        assertEquals(1, components.size());
        Component link = find(components.get(0), "Retour");
        assertNotNull(link);
        assertEquals(ClickEvent.runCommand("/spawn"), link.clickEvent());
        HoverEvent<?> hover = link.hoverEvent();
        assertNotNull(hover);
        assertEquals("Retour au spawn", PlainTextComponentSerializer.plainText().serialize((Component) hover.value()));
    }

    @Test
    void values_arePassedApart_andInsertedAsPlainText() {
        Map<MessageKey, Map<String, String>> seen = new HashMap<>();
        ComponentMessageFormatter<MockSender> templates = ComponentMessageFormatter.miniMessage(
                key -> key == MessageKey.ARG_NOT_RECOGNIZED ? "<gray>Inconnu : <yellow><arg>" : null);
        install((receiver, key, placeholders) -> {
            seen.put(key, placeholders);
            return templates.format(receiver, key, placeholders);
        });
        Command<Object, MockSender> command = command("give");
        command.addArgs("amount", Integer.class);
        manager.registerCommand(command);

        manager.getInvoker().invoke(sender, "give", new String[]{"<red>dix"});

        assertEquals(Map.of(MessageKey.ARG, "<red>dix"), seen.get(MessageKey.ARG_NOT_RECOGNIZED));
        assertEquals("Inconnu : <red>dix", PlainTextComponentSerializer.plainText().serialize(components.get(0)));
    }

    @Test
    void requirementValue_isTheErrorMessage_orTheName() {
        Map<String, String> values = new HashMap<>();
        install((receiver, key, placeholders) -> {
            values.putAll(placeholders);
            return Component.empty();
        });
        Command<Object, MockSender> command = command("nether");
        command.addRequirements(new Requirement<>() {
            @Override
            public boolean check(MockSender s) {
                return false;
            }

            @Override
            public String errorMessage() {
                return "Va dans le Nether.";
            }
        });
        manager.registerCommand(command);

        manager.getInvoker().invoke(sender, "nether", new String[0]);

        assertEquals(Map.of(MessageKey.REQUIREMENT_VALUE, "Va dans le Nether."), values);
    }

    @Test
    void nullComponent_fallsBackOnTheTextPath() {
        install((receiver, key, placeholders) -> null);
        manager.setMessageFormatter((s, raw) -> "[p] " + raw);
        Command<Object, MockSender> command = command("admin");
        command.setPermission("admin.use");
        manager.registerCommand(command);

        manager.getInvoker().invoke(sender, "admin", new String[0]);

        assertTrue(components.isEmpty());
        assertEquals(List.of("[p] &cYou do not have permission to use this command."), texts);
    }

    @Test
    void removingTheFormatter_restoresTheTextPath() {
        install((receiver, key, placeholders) -> Component.text("x"));
        assertNotNull(ComponentMessages.formatterOf(manager));
        ComponentMessages.install(manager, null, (s, c) -> components.add(c));
        assertNull(manager.getMessageDispatcher());
    }

    @Test
    void handlerTemplates_mapTheLegacyPlaceholders() {
        ComponentMessageFormatter<MockSender> formatter = ComponentMessageFormatter.miniMessage(
                new fr.traqueur.commands.impl.logging.InternalMessageHandler() {
                    @Override
                    public String getArgNotRecognized() {
                        return "<red>Argument <yellow>%arg%</yellow> inconnu";
                    }
                });

        Component component = formatter.format(sender, MessageKey.ARG_NOT_RECOGNIZED, Map.of(MessageKey.ARG, "abc"));

        assertEquals("Argument abc inconnu", PlainTextComponentSerializer.plainText().serialize(component));
        assertNull(formatter.format(sender, MessageKey.INTERNAL_ERROR, Map.of()));
    }

    @Test
    void absentValues_areEmpty_soOneTemplateServesEveryKey() {
        ComponentMessageFormatter<MockSender> formatter = ComponentMessageFormatter.miniMessage(key -> key + ":<arg><usage><requirement>");

        Component usage = formatter.format(sender, MessageKey.USAGE, Map.of(MessageKey.USAGE_VALUE, "/give <amount>"));

        assertEquals("USAGE:/give <amount>", PlainTextComponentSerializer.plainText().serialize(usage));
    }

    private void install(ComponentMessageFormatter<MockSender> formatter) {
        ComponentMessages.install(manager, formatter, (receiver, component) -> components.add(component));
    }

    private static Component find(Component root, String content) {
        if (root instanceof net.kyori.adventure.text.TextComponent text && text.content().equals(content)) {
            return root;
        }
        for (Component child : root.children()) {
            Component found = find(child, content);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static Command<Object, MockSender> command(String name) {
        return new Command<>(null, name) {
            @Override
            public void execute(MockSender s, Arguments arguments) {
            }
        };
    }
}
