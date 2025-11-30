package fr.traqueur.commands.test.commands;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.jda.Command;
import fr.traqueur.commands.jda.JDAArguments;
import fr.traqueur.commands.test.TestBot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Simple ping command that responds with "Pong!" and the bot's latency.
 */
public class PingCommand extends Command<TestBot> {

    public PingCommand(TestBot bot) {
        super(bot, "ping");
        this.setDescription("Check the bot's latency");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
        long gatewayPing = event.getJDA().getGatewayPing();

        event.reply("Pong! Gateway ping: " + gatewayPing + "ms").queue(response -> {
            response.retrieveOriginal().queue(message -> {
                long restPing = message.getTimeCreated().toInstant().toEpochMilli() -
                               event.getTimeCreated().toInstant().toEpochMilli();
                response.editOriginal("Pong! Gateway ping: " + gatewayPing + "ms | REST ping: " + restPing + "ms").queue();
            });
        });
    }
}