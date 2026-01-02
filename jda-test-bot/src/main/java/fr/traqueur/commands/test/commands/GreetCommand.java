package fr.traqueur.commands.test.commands;

import fr.traqueur.commands.api.arguments.Infinite;
import fr.traqueur.commands.jda.Command;
import fr.traqueur.commands.jda.JDAArguments;
import fr.traqueur.commands.jda.JDAInteractionContext;
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
        this.addOptionalArgs("message", Infinite.class);
    }

    @Override
    public void execute(JDAInteractionContext context, JDAArguments arguments) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        User target = arguments.get("user");

        String customMessage = arguments.<String>getOptional("message").orElse("Hello there!");

        String greeting = String.format("%s says to %s: %s",
                event.getUser().getAsMention(),
                target.getAsMention(),
                customMessage);

        arguments.reply(greeting);
    }
}
