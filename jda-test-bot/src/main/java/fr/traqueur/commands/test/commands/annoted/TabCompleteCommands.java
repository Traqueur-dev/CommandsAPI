package fr.traqueur.commands.test.commands.annoted;

import fr.traqueur.commands.annotations.*;
import fr.traqueur.commands.jda.JDAInteractionContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandContainer
public class TabCompleteCommands {

    @Command(name = "color", description = "Choose a color and see it displayed")
    @Alias(value = {"colour"})
    public void color(JDAInteractionContext context, @Arg("color") String colorName) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        Color color;
        String colorDisplay;

        switch (colorName.toLowerCase()) {
            case "red":
                color = Color.RED;
                colorDisplay = "Red 🔴";
                break;
            case "blue":
                color = Color.BLUE;
                colorDisplay = "Blue 🔵";
                break;
            case "green":
                color = Color.GREEN;
                colorDisplay = "Green 🟢";
                break;
            case "yellow":
                color = Color.YELLOW;
                colorDisplay = "Yellow 🟡";
                break;
            case "purple":
                color = new Color(128, 0, 128);
                colorDisplay = "Purple 🟣";
                break;
            case "orange":
                color = Color.ORANGE;
                colorDisplay = "Orange 🟠";
                break;
            default:
                event.reply("Invalid color! Choose from: red, blue, green, yellow, purple, orange")
                        .setEphemeral(true)
                        .queue();
                return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Color Display")
                .setDescription("You selected: " + colorDisplay)
                .setColor(color);

        event.replyEmbeds(embed.build()).queue();
    }

    @TabComplete(command = "color", arg = "color")
    public List<String> completeColor(JDAInteractionContext context, String current) {
        List<String> colors = Arrays.asList("red", "blue", "green", "yellow", "purple", "orange");
        return colors.stream()
                .filter(c -> c.startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Command(name = "language", description = "Set your preferred language")
    public void language(JDAInteractionContext context, @Arg("lang") String language) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        String message;
        switch (language.toLowerCase()) {
            case "english":
                message = "Language set to English! 🇬🇧";
                break;
            case "french":
                message = "Langue définie sur Français! 🇫🇷";
                break;
            case "spanish":
                message = "¡Idioma establecido en Español! 🇪🇸";
                break;
            case "german":
                message = "Sprache auf Deutsch eingestellt! 🇩🇪";
                break;
            case "japanese":
                message = "言語を日本語に設定しました！🇯🇵";
                break;
            default:
                event.reply("Unsupported language!").setEphemeral(true).queue();
                return;
        }
        event.reply(message).queue();
    }

    @TabComplete(command = "language", arg = "lang")
    public List<String> completeLanguage(JDAInteractionContext context, String current) {
        return Arrays.asList("english", "french", "spanish", "german", "japanese").stream()
                .filter(lang -> lang.startsWith(current.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Command(name = "category", description = "Browse categories")
    public void category(JDAInteractionContext context, @Arg("name") String categoryName) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) context.getEvent();
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Category: " + categoryName)
                .setDescription("Viewing category: " + categoryName)
                .setColor(Color.CYAN);

        event.replyEmbeds(embed.build()).queue();
    }

    @TabComplete(command = "category", arg = "name")
    public List<String> completeCategory() {
        return Arrays.asList("gaming", "music", "art", "programming", "sports", "movies");
    }
}
