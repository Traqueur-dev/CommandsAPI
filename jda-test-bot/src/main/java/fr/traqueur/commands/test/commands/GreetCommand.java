package fr.traqueur.commands.test.commands;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.jda.Command;
import fr.traqueur.commands.jda.JDAArguments;
import fr.traqueur.commands.test.TestBot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Greet command that greets a user with an optional custom message.
 * Usage: /greet <user> [message]
 */
public class GreetCommand extends Command<TestBot> {

    public GreetCommand(TestBot bot) {
        super(bot, "greet");
        this.setDescription("Greet a user with a custom message");
        this.addArgs("user", User.class);
        this.addOptionalArgs("message:infinite");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
        User target = arguments.getUser("user").orElse(null);
        if (target == null) {
            arguments.replyEphemeral("User not found!");
            return;
        }

        String customMessage = arguments.getAsString("message", "Hello there!");

        String greeting = String.format("%s says to %s: %s",
                event.getUser().getAsMention(),
                target.getAsMention(),
                customMessage);

        arguments.reply(greeting);
    }
}