package fr.traqueur.commands.test.commands.annoted;

import fr.traqueur.commands.annotations.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CommandContainer
public class TestAnnotedCommands {

    @Command(name = "testannoted", description = "A test annoted command", usage = "/testannoted")
    @Alias(value = {"ta", "testa"})
    public void testAnnotedCommand(SlashCommandInteractionEvent event, @Arg("arg1") int argument1, @Arg("arg2") Optional<String> argument2) {
        String arg2Value = argument2.orElse("no value provided");
        event.reply("You executed the testannoted command with arg1: " + argument1 + " and arg2: " + arg2Value).setEphemeral(true).queue();
    }

    @TabComplete(command="testannoted", arg="arg2")
    public List<String> tabCompleteArg2(SlashCommandInteractionEvent event, String currentInput) {
        List<String> suggestions = Arrays.asList("option1", "option2", "option3");
        return suggestions.stream()
                .filter(option -> option.startsWith(currentInput))
                .collect(Collectors.toList());
    }

}
