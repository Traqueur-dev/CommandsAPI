package fr.traqueur.commands.api.models;

import fr.traqueur.commands.api.requirements.Requirement;
import fr.traqueur.commands.test.mocks.MockCommandManager;
import fr.traqueur.commands.test.mocks.MockSender;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequirementMessageTest {

    @Test
    void emptyErrorMessage_fillsThePlaceholderWithTheName() {
        List<String> received = invokeWith(new Requirement<>() {
            @Override
            public boolean check(MockSender sender) {
                return false;
            }

            @Override
            public String errorMessage() {
                return "";
            }

            @Override
            public String name() {
                return "être dans le Nether";
            }
        });

        assertEquals(List.of("The requirement être dans le Nether was not met"), received);
    }

    @Test
    void defaultName_isTheSimpleClassName() {
        assertEquals(List.of("The requirement InNether was not met"), invokeWith(new InNether()));
    }

    @Test
    void errorMessage_isStillSentAsIs() {
        InNether requirement = new InNether() {
            @Override
            public String errorMessage() {
                return "Go to the Nether first.";
            }
        };

        assertEquals(List.of("Go to the Nether first."), invokeWith(requirement));
    }

    private static List<String> invokeWith(Requirement<MockSender> requirement) {
        MockCommandManager manager = new MockCommandManager();
        Command<Object, MockSender> command = new Command<>(null, "nether") {
            @Override
            public void execute(MockSender sender, fr.traqueur.commands.api.arguments.Arguments arguments) {
            }
        };
        command.addRequirements(requirement);
        manager.registerCommand(command);

        List<String> received = new ArrayList<>();
        MockSender sender = new MockSender() {
            @Override
            public void sendMessage(String message) {
                received.add(message);
            }

            @Override
            public boolean hasPermission(String permission) {
                return true;
            }
        };
        manager.getInvoker().invoke(sender, "nether", new String[0]);
        return received;
    }

    static class InNether implements Requirement<MockSender> {
        @Override
        public boolean check(MockSender sender) {
            return false;
        }

        @Override
        public String errorMessage() {
            return "";
        }
    }
}
