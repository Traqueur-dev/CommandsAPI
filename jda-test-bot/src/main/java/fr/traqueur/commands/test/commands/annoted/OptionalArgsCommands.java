package fr.traqueur.commands.test.commands.annoted;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import fr.traqueur.commands.jda.JDAInteractionContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.Color;
import java.time.Instant;
import java.util.Optional;

@CommandContainer
public class OptionalArgsCommands {

    @Command(name = "announce", description = "Make an announcement")
    public void announce(JDAInteractionContext context,
                        @Arg("message") String message,
                        @Arg("title") Optional<String> title,
                        @Arg("color") Optional<String> colorHex) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        Color color;
        try {
            color = colorHex.map(hex -> Color.decode(hex.startsWith("#") ? hex : "#" + hex))
                    .orElse(Color.BLUE);
        } catch (NumberFormatException e) {
            color = Color.BLUE;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title.orElse("Announcement"))
                .setDescription(message)
                .setColor(color)
                .setTimestamp(Instant.now())
                .setFooter("Announced by " + event.getUser().getName(), event.getUser().getEffectiveAvatarUrl());

        event.reply("Announcement sent!").setEphemeral(true).queue();
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    @Command(name = "poll", description = "Create a simple poll")
    public void poll(JDAInteractionContext context,
                    @Arg("question") String question,
                    @Arg("duration") Optional<Integer> durationMinutes) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        int duration = durationMinutes.orElse(5);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("📊 Poll")
                .setDescription(question)
                .setColor(Color.ORANGE)
                .setFooter("Poll will close in " + duration + " minutes")
                .setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue(response -> {
            response.retrieveOriginal().queue(message -> {
                message.addReaction(Emoji.fromUnicode("👍")).queue();
                message.addReaction(Emoji.fromUnicode("👎")).queue();
            });
        });
    }

    @Command(name = "remind", description = "Set a reminder")
    public void remind(JDAInteractionContext context,
                      @Arg("message") String message,
                      @Arg("minutes") Optional<Integer> minutes) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        int time = minutes.orElse(5);
        event.reply("⏰ I'll remind you in " + time + " minutes about: " + message)
                .setEphemeral(true)
                .queue();
        // Note: Actual reminder implementation would require a scheduler
    }
}
