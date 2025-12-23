package fr.traqueur.commands.test.commands;

import fr.traqueur.commands.jda.Command;
import fr.traqueur.commands.jda.JDAArguments;
import fr.traqueur.commands.test.TestBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Command that displays information about a user.
 */
public class UserInfoCommand extends Command<TestBot> {

    public UserInfoCommand(TestBot bot) {
        super(bot, "userinfo");
        this.setDescription("Get information about a user");
        this.setGameOnly(true); // Guild-only command
        this.addOptionalArgs("user", Member.class);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, JDAArguments arguments) {
        JDAArguments jdaArgs = jda(arguments);

        // Get the target user (defaults to the command executor)
        Member member = jdaArgs.<Member>getOptional("user").orElse(event.getMember());
        if (member == null) {
            jdaArgs.reply("User not found in this server!");
            return;
        }
        User user = member.getUser();

        // Build embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("User Information")
                .setThumbnail(user.getEffectiveAvatarUrl())
                .setColor(member.getColor() != null ? member.getColor() : Color.BLUE)
                .addField("Username", user.getName(), true)
                .addField("Display Name", member.getEffectiveName(), true)
                .addField("ID", user.getId(), true)
                .addField("Account Created", user.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                .addField("Joined Server", member.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME), false)
                .addField("Roles", member.getRoles().isEmpty() ? "None" :
                        member.getRoles().stream().map(IMentionable::getAsMention).limit(10)
                                .reduce((a, b) -> a + ", " + b).orElse("None"), false)
                .setFooter("Requested by " + event.getUser().getName(), event.getUser().getEffectiveAvatarUrl());

        event.replyEmbeds(embed.build()).queue();
    }
}