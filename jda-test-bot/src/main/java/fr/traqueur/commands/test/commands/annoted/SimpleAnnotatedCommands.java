package fr.traqueur.commands.test.commands.annoted;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import fr.traqueur.commands.jda.JDAInteractionContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.Color;
import java.time.Instant;

@CommandContainer
public class SimpleAnnotatedCommands {

    @Command(name = "echo", description = "Echo back a message")
    public void echo(JDAInteractionContext context, @Arg("message") String message) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        event.reply("You said: " + message).setEphemeral(true).queue();
    }

    @Command(name = "serverinfo", description = "Get information about this server")
    public void serverInfo(JDAInteractionContext context) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        if (event.getGuild() == null) {
            event.reply("This command can only be used in a server!").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Server Information")
                .setColor(Color.BLUE)
                .addField("Server Name", event.getGuild().getName(), true)
                .addField("Server ID", event.getGuild().getId(), true)
                .addField("Owner", event.getGuild().getOwner() != null ? event.getGuild().getOwner().getAsMention() : "Unknown", true)
                .addField("Member Count", String.valueOf(event.getGuild().getMemberCount()), true)
                .addField("Boost Level", String.valueOf(event.getGuild().getBoostTier()), true)
                .addField("Created", event.getGuild().getTimeCreated().toString(), false)
                .setThumbnail(event.getGuild().getIconUrl())
                .setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
    }

    @Command(name = "avatar", description = "Get a user's avatar")
    public void avatar(JDAInteractionContext context, @Arg("user") net.dv8tion.jda.api.entities.User user) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        String avatarUrl = user.getEffectiveAvatarUrl() + "?size=512";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(user.getName() + "'s Avatar")
                .setImage(avatarUrl)
                .setColor(Color.CYAN)
                .setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
    }

    @Command(name = "roll", description = "Roll a dice")
    public void roll(JDAInteractionContext context, @Arg("sides") int sides) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        if (sides < 2 || sides > 1000) {
            event.reply("Please choose a dice with 2-1000 sides!").setEphemeral(true).queue();
            return;
        }

        int result = (int) (Math.random() * sides) + 1;
        event.reply("🎲 You rolled a **" + result + "** (d" + sides + ")").queue();
    }

    @Command(name = "coinflip", description = "Flip a coin")
    public void coinFlip(JDAInteractionContext context) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        String result = Math.random() < 0.5 ? "Heads" : "Tails";
        event.reply("🪙 The coin landed on: **" + result + "**").queue();
    }
}
