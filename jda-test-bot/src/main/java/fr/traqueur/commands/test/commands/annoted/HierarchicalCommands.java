package fr.traqueur.commands.test.commands.annoted;

import fr.traqueur.commands.annotations.Arg;
import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import fr.traqueur.commands.jda.JDAInteractionContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.Color;
import java.time.Instant;
import java.util.Optional;

@CommandContainer
public class HierarchicalCommands {

    // Moderation commands - using dotted names for hierarchy
    @Command(name = "moderation.timeout", description = "Timeout a user", permission = "MODERATE_MEMBERS")
    public void moderationTimeout(JDAInteractionContext context,
                                 @Arg("user") Member member,
                                 @Arg("minutes") int minutes,
                                 @Arg("reason") Optional<String> reason) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        String timeoutReason = reason.orElse("No reason provided");

        if (minutes < 1 || minutes > 40320) { // Discord max is 28 days
            event.reply("Timeout duration must be between 1 minute and 28 days (40320 minutes)!")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⏱️ User Timed Out")
                .setColor(Color.ORANGE)
                .addField("User", member.getAsMention(), true)
                .addField("Duration", minutes + " minutes", true)
                .addField("Reason", timeoutReason, false)
                .addField("Moderator", event.getUser().getAsMention(), true)
                .setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
        // Note: Actual timeout would require: member.timeoutFor(Duration.ofMinutes(minutes)).queue();
    }

    @Command(name = "moderation.warn", description = "Warn a user", permission = "MODERATE_MEMBERS")
    public void moderationWarn(JDAInteractionContext context,
                              @Arg("user") Member member,
                              @Arg("reason") Optional<String> reason) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        String warnReason = reason.orElse("No reason provided");

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("⚠️ User Warned")
                .setColor(Color.YELLOW)
                .addField("User", member.getAsMention(), true)
                .addField("Reason", warnReason, false)
                .addField("Moderator", event.getUser().getAsMention(), true)
                .setTimestamp(Instant.now());

        event.replyEmbeds(embed.build()).queue();
    }

    @Command(name = "moderation.clear", description = "Clear messages", permission = "MANAGE_MESSAGES")
    public void moderationClear(JDAInteractionContext context, @Arg("amount") int amount) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        if (amount < 1 || amount > 100) {
            event.reply("Amount must be between 1 and 100!").setEphemeral(true).queue();
            return;
        }

        event.reply("Would clear " + amount + " messages (not implemented in test bot)")
                .setEphemeral(true)
                .queue();
    }

    // Configuration commands - multi-level hierarchy
    @Command(name = "botconfig.prefix", description = "Change bot prefix")
    public void configPrefix(JDAInteractionContext context, @Arg("new_prefix") String newPrefix) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        event.reply("✅ Prefix changed to: `" + newPrefix + "` (not actually saved in test bot)")
                .setEphemeral(true)
                .queue();
    }

    @Command(name = "botconfig.language", description = "Change bot language")
    public void configLanguage(JDAInteractionContext context, @Arg("lang") String language) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        event.reply("✅ Language changed to: " + language + " (not actually saved in test bot)")
                .setEphemeral(true)
                .queue();
    }

    @Command(name = "botconfig.reset", description = "Reset all settings")
    public void configReset(JDAInteractionContext context) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        event.reply("✅ All settings have been reset to defaults! (not actually reset in test bot)")
                .setEphemeral(true)
                .queue();
    }

    // Deep hierarchy example
    @Command(name = "system.server.restart", description = "Restart the server", permission = "ADMINISTRATOR")
    public void systemServerRestart(JDAInteractionContext context) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        event.reply("🔄 Server restart initiated! (not actually restarting in test bot)")
                .setEphemeral(true)
                .queue();
    }

    @Command(name = "system.server.backup", description = "Create a server backup", permission = "ADMINISTRATOR")
    public void systemServerBackup(JDAInteractionContext context) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        event.reply("💾 Creating server backup... (not actually creating backup in test bot)")
                .setEphemeral(true)
                .queue();
    }

    @Command(name = "system.logs.view", description = "View server logs", permission = "ADMINISTRATOR")
    public void systemLogsView(JDAInteractionContext context,
                              @Arg("lines") Optional<Integer> lines) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        int numLines = lines.orElse(10);
        event.reply("📋 Viewing last " + numLines + " log lines... (not implemented in test bot)")
                .setEphemeral(true)
                .queue();
    }

    @Command(name = "system.logs.clear", description = "Clear server logs", permission = "ADMINISTRATOR")
    public void systemLogsClear(JDAInteractionContext context) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        event.reply("🗑️ Server logs cleared! (not actually cleared in test bot)")
                .setEphemeral(true)
                .queue();
    }
}
