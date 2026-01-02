package fr.traqueur.commands.test;

import fr.traqueur.commands.annotations.AnnotationCommandProcessor;
import fr.traqueur.commands.jda.CommandManager;
import fr.traqueur.commands.test.commands.*;
import fr.traqueur.commands.test.commands.annoted.TestAnnotedCommands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.logging.Logger;

/**
 * Test bot to demonstrate the JDA CommandsAPI.
 * <p>
 * To run this bot:
 * 1. Set the DISCORD_BOT_TOKEN environment variable
 * 2. Set the DISCORD_GUILD_ID environment variable (optional, for testing)
 * 3. Run with: ./gradlew :jda-test-bot:runBot
 */
public class TestBot {

    private static final Logger LOGGER = Logger.getLogger(TestBot.class.getName());

    public TestBot(String token) throws InterruptedException {
        LOGGER.info("Starting Discord bot...");

        // Build JDA instance
        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .build()
                .awaitReady();

        LOGGER.info("Bot is ready! Logged in as: " + jda.getSelfUser().getAsTag());

        // Create command manager
        CommandManager<TestBot> commandManager = new CommandManager<>(this, jda, LOGGER);
        commandManager.setDebug(true);

        AnnotationCommandProcessor<TestBot, SlashCommandInteractionEvent> annotationProcessor =
                new AnnotationCommandProcessor<>(commandManager);

        // Register commands
        LOGGER.info("Registering commands...");
        annotationProcessor.register(new TestAnnotedCommands());

        commandManager.registerCommand(new PingCommand(this));
        commandManager.registerCommand(new UserInfoCommand(this));
        commandManager.registerCommand(new MathCommand(this));
        commandManager.registerCommand(new GreetCommand(this));
        commandManager.registerCommand(new AdminCommand(this));

        // Sync commands
        String guildId = System.getenv("DISCORD_GUILD_ID");
        if (guildId != null && !guildId.isEmpty()) {
            LOGGER.info("Syncing commands to guild " + guildId + " (instant update)...");
            commandManager.syncCommandsToGuild(guildId);
        } else {
            LOGGER.info("Syncing commands globally (may take up to 1 hour)...");
            commandManager.syncCommands();
        }

        LOGGER.info("Bot is fully operational!");
    }

    public static void main(String[] args) {
        String token = System.getenv("DISCORD_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            LOGGER.severe("DISCORD_BOT_TOKEN environment variable not set!");
            LOGGER.info("Please set your Discord bot token with: export DISCORD_BOT_TOKEN=your_token_here");
            return;
        }

        try {
            new TestBot(token);
        } catch (Exception e) {
            LOGGER.severe("Failed to start bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}