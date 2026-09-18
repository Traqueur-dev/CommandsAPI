package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandPlatform;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The JDA executor answers the interaction directly instead of going through the platform,
 * so it needs its own coverage of the message formatter.
 */
@SuppressWarnings("unchecked")
class JDAExecutorFormatterTest {

    private CommandManager<Object, JDAInteractionContext> manager;
    private JDAExecutor<Object> executor;
    private SlashCommandInteractionEvent event;

    @BeforeEach
    void setup() {
        CommandPlatform<Object, JDAInteractionContext> platform = mock(CommandPlatform.class);
        manager = new CommandManager<>(platform) {
        };
        executor = new JDAExecutor<>(manager);

        event = mock(SlashCommandInteractionEvent.class);
        ReplyCallbackAction action = mock(ReplyCallbackAction.class);
        when(event.reply(anyString())).thenReturn(action);
        when(action.setEphemeral(anyBoolean())).thenReturn(action);
    }

    @Test
    void noFormatter_replyIsSentUnchanged() {
        when(event.getName()).thenReturn("unknown");

        executor.onSlashCommandInteraction(event);

        verify(event).reply("Command not found!");
    }

    @Test
    void formatter_transformsHardcodedReplyAndSeesTheInteraction() {
        List<JDAInteractionContext> recipients = new ArrayList<>();
        manager.setMessageFormatter((sender, raw) -> {
            recipients.add(sender);
            return "[prefix] " + raw;
        });
        when(event.getName()).thenReturn("unknown");

        executor.onSlashCommandInteraction(event);

        verify(event).reply("[prefix] Command not found!");
        assertEquals(1, recipients.size());
        assertEquals(event, recipients.get(0).getEvent());
    }

    @Test
    void formatter_transformsMessageHandlerReply() {
        manager.setMessageFormatter((sender, raw) -> "[prefix] " + raw);
        DummyCommand command = new DummyCommand();
        command.setEnabled(false);
        manager.getCommands().addCommand("base", command);
        when(event.getName()).thenReturn("base");

        executor.onSlashCommandInteraction(event);

        verify(event).reply("[prefix] " + manager.getMessageHandler().getCommandDisabledMessage());
    }

    static class DummyCommand extends Command<Object, JDAInteractionContext> {
        DummyCommand() {
            super(null, "base");
        }

        @Override
        public void execute(JDAInteractionContext sender, Arguments args) {
        }
    }
}
