package fr.traqueur.commands.annotations;

import fr.traqueur.commands.annotations.commands.DefaultArgsTestCommands;
import fr.traqueur.commands.annotations.commands.InvalidDefaultTestCommands;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.test.mocks.MockCommandManager;
import fr.traqueur.commands.test.mocks.MockSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DefaultArgsTest {

    private MockCommandManager manager;
    private DefaultArgsTestCommands commands;
    private final MockSender sender = new MockSender() {
        @Override
        public void sendMessage(String message) {
        }

        @Override
        public boolean hasPermission(String permission) {
            return true;
        }
    };

    @BeforeEach
    void setUp() {
        manager = new MockCommandManager();
        commands = new DefaultArgsTestCommands();
        new AnnotationCommandProcessor<>(manager).register(commands);
    }

    @Test
    void defaultArgument_isOptional() {
        Command<Object, MockSender> give = manager.getMockPlatform().getCommand("give");
        assertEquals(1, give.getArgs().size());
        assertEquals("amount", give.getOptionalArgs().get(0).name());
    }

    @Test
    void leftOut_receivesTheConvertedDefault_givenWins() {
        manager.getInvoker().invoke(sender, "give", new String[]{"apple"});
        manager.getInvoker().invoke(sender, "give", new String[]{"apple", "5"});

        assertEquals(List.of("applex1", "applex5"), commands.received);
    }

    @Test
    void optionalWithDefault_isNeverEmpty() {
        manager.getInvoker().invoke(sender, "heal", new String[0]);
        manager.getInvoker().invoke(sender, "heal", new String[]{"3.5"});

        assertEquals(List.of(Optional.of(20.0), Optional.of(3.5)), commands.received);
    }

    @Test
    void infiniteWithDefault_receivesTheText() {
        manager.getInvoker().invoke(sender, "say", new String[0]);
        manager.getInvoker().invoke(sender, "say", new String[]{"good", "bye"});

        assertEquals(List.of("hello there", "good bye"), commands.received);
    }

    @Test
    void invalidPrimitiveDefault_isRefusedAtRegistration() {
        AnnotationCommandProcessor<Object, MockSender> processor = new AnnotationCommandProcessor<>(new MockCommandManager());
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> processor.register(new InvalidDefaultTestCommands()));
        assertTrue(error.getMessage().contains("lots"));
    }
}
