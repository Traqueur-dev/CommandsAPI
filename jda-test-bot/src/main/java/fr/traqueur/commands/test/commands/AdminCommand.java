package fr.traqueur.commands.test.commands;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.jda.Command;
import fr.traqueur.commands.jda.JDAArguments;
import fr.traqueur.commands.test.TestBot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Admin command with subcommand groups to test JDAPlatform.addCommand() with parts.length >= 3
 * Structure:
 * - /admin users kick <user> <reason>
 * - /admin users ban <user> <reason>
 * - /admin server info
 * - /admin server settings <option> <value>
 */
public class AdminCommand extends Command<TestBot> {

    public AdminCommand(TestBot bot) {
        super(bot, "admin");
        this.setDescription("Administration commands");
        this.setGameOnly(true);

        // Users group
        Command<TestBot> usersGroup = new UsersGroupCommand(bot);
        usersGroup.addSubCommand(new KickCommand(bot));
        usersGroup.addSubCommand(new BanCommand(bot));

        // Server group
        Command<TestBot> serverGroup = new ServerGroupCommand(bot);
        serverGroup.addSubCommand(new ServerInfoCommand(bot));
        serverGroup.addSubCommand(new ServerSettingsCommand(bot));

        this.addSubCommand(usersGroup);
        this.addSubCommand(serverGroup);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
        // This won't be called since we have subcommands
    }

    /**
     * Users group: /admin users
     */
    private static class UsersGroupCommand extends Command<TestBot> {
        public UsersGroupCommand(TestBot bot) {
            super(bot, "users");
            this.setDescription("User management commands");
        }

        @Override
        public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
            // This won't be called since we have subcommands
        }
    }

    /**
     * Kick subcommand: /admin users kick <user> <reason>
     */
    private static class KickCommand extends Command<TestBot> {
        public KickCommand(TestBot bot) {
            super(bot, "kick");
            this.setDescription("Kick a user from the server");
            this.addArgs("user", User.class);
            this.addOptionalArgs("reason", String.class);
        }

        @Override
        public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
            JDAArguments jdaArgs = jda(arguments);
            User user = jdaArgs.getUser("user").orElse(null);
            String reason = jdaArgs.getAsString("reason").orElse("No reason provided");

            if (user == null) {
                jdaArgs.replyEphemeral("User not found!");
                return;
            }

            jdaArgs.reply(String.format("Would kick user %s for reason: %s",
                user.getAsMention(), reason));
        }
    }

    /**
     * Ban subcommand: /admin users ban <user> <reason>
     */
    private static class BanCommand extends Command<TestBot> {
        public BanCommand(TestBot bot) {
            super(bot, "ban");
            this.setDescription("Ban a user from the server");
            this.addArgs("user", User.class);
            this.addOptionalArgs("reason", String.class);
        }

        @Override
        public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
            User user = arguments.getUser("user").orElse(null);
            String reason = arguments.getAsString("reason").orElse("No reason provided");

            if (user == null) {
                arguments.replyEphemeral("User not found!");
                return;
            }

            arguments.reply(String.format("Would ban user %s for reason: %s",
                user.getAsMention(), reason));
        }
    }

    /**
     * Server group: /admin server
     */
    private static class ServerGroupCommand extends Command<TestBot> {
        public ServerGroupCommand(TestBot bot) {
            super(bot, "server");
            this.setDescription("Server management commands");
        }

        @Override
        public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
            // This won't be called since we have subcommands
        }
    }

    /**
     * Server info subcommand: /admin server info
     */
    private static class ServerInfoCommand extends Command<TestBot> {
        public ServerInfoCommand(TestBot bot) {
            super(bot, "info");
            this.setDescription("Display server information");
        }

        @Override
        public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
            JDAArguments jdaArgs = jda(arguments);

            if (event.getGuild() == null) {
                jdaArgs.replyEphemeral("This command can only be used in a server!");
                return;
            }

            String info = String.format(
                    """
                            **Server Info**
                            Name: %s
                            ID: %s
                            Members: %d
                            Owner: %s""",
                event.getGuild().getName(),
                event.getGuild().getId(),
                event.getGuild().getMemberCount(),
                event.getGuild().getOwner() != null ? event.getGuild().getOwner().getAsMention() : "Unknown"
            );

            jdaArgs.reply(info);
        }
    }

    /**
     * Server settings subcommand: /admin server settings <option> <value>
     */
    private static class ServerSettingsCommand extends Command<TestBot> {
        public ServerSettingsCommand(TestBot bot) {
            super(bot, "settings");
            this.setDescription("Modify server settings");
            this.addArgs("option", String.class, "value", String.class);
        }

        @Override
        public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
            JDAArguments jdaArgs = jda(arguments);
            String option = jdaArgs.getAsString("option").orElse("unknown");
            String value = jdaArgs.getAsString("value").orElse("unknown");

            jdaArgs.reply(String.format("Would set setting '%s' to '%s'", option, value));
        }
    }
}