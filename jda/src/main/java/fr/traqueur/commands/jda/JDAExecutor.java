package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.api.parsing.ParseResult;
import fr.traqueur.commands.api.requirements.Requirement;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * JDA executor that handles slash command and autocomplete events.
 */
public class JDAExecutor<T> extends ListenerAdapter {

    private final CommandManager<T, JDAInteractionContext> commandManager;
    private final JDAArgumentParser<T> parser;

    public JDAExecutor(CommandManager<T, JDAInteractionContext> commandManager) {
        this.commandManager = commandManager;
        this.parser = new JDAArgumentParser<>(commandManager.getLogger());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String label = buildLabel(event);

        if (commandManager.isDebug()) {
            commandManager.getLogger().info("Received slash command: " + label);
        }

        // Wrap the event
        JDAInteractionContext context = JDAInteractionContext.wrap(event);

        // Find command
        String[] labelParts = label.split("\\.");
        Optional<CommandTree.MatchResult<T, JDAInteractionContext>> found =
                commandManager.getCommands().findNode(labelParts);

        if (found.isEmpty()) {
            event.reply("Command not found!").setEphemeral(true).queue();
            return;
        }

        Command<T, JDAInteractionContext> command = found.get().node().getCommand().orElse(null);
        if (command == null) {
            event.reply("Command implementation not found!").setEphemeral(true).queue();
            return;
        }

        // Validate
        if (!validateCommand(context, event, command)) {
            return;
        }

        // Parse & Execute
        ParseResult result = parser.parse(command, event);

        if (result.isError()) {
            String msg = commandManager.getMessageHandler().getArgNotRecognized()
                    .replace("%arg%", result.error().argumentName() != null ? result.error().argumentName() : "unknown");
            event.reply(msg).setEphemeral(true).queue();
            return;
        }

        try {
            command.execute(context, result.arguments());
        } catch (Exception e) {
            commandManager.getLogger().error("Error executing command " + label + ": " + e.getMessage());
            if (!event.isAcknowledged()) {
                event.reply("An error occurred!").setEphemeral(true).queue();
            }
        }
    }

    private boolean validateCommand(JDAInteractionContext context,
                                    SlashCommandInteractionEvent event,
                                    Command<T, JDAInteractionContext> command) {
        // Enabled check
        if (!command.isEnabled()) {
            event.reply(commandManager.getMessageHandler().getCommandDisabledMessage())
                    .setEphemeral(true).queue();
            return false;
        }

        // Game-only check
        if (command.inGameOnly() && !event.isFromGuild()) {
            event.reply(commandManager.getMessageHandler().getOnlyInGameMessage())
                    .setEphemeral(true).queue();
            return false;
        }

        // Permission check
        String perm = command.getPermission();
        if (!perm.isEmpty() && !commandManager.getPlatform().hasPermission(context, perm)) {
            event.reply(commandManager.getMessageHandler().getNoPermissionMessage())
                    .setEphemeral(true).queue();
            return false;
        }

        // Requirements check
        for (Requirement<JDAInteractionContext> req : command.getRequirements()) {
            if (!req.check(context)) {
                String msg = req.errorMessage().isEmpty()
                        ? commandManager.getMessageHandler().getRequirementMessage()
                        .replace("%requirement%", req.getClass().getSimpleName())
                        : req.errorMessage();
                event.reply(msg).setEphemeral(true).queue();
                return false;
            }
        }

        return true;
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        String label = buildLabel(event);
        String focusedOptionName = event.getFocusedOption().getName();

        if (commandManager.isDebug()) {
            commandManager.getLogger().info("Received autocomplete for: " + label + " arg: " + focusedOptionName);
        }

        // Wrap the event
        JDAInteractionContext context = JDAInteractionContext.wrap(event);

        // Find command
        String[] labelParts = label.split("\\.");
        Optional<CommandTree.MatchResult<T, JDAInteractionContext>> found =
                commandManager.getCommands().findNode(labelParts);

        if (found.isEmpty()) {
            event.replyChoices(List.of()).queue();
            return;
        }

        Command<T, JDAInteractionContext> command = found.get().node().getCommand().orElse(null);
        if (command == null) {
            event.replyChoices(List.of()).queue();
            return;
        }

        // Find the argument being completed
        Argument<JDAInteractionContext> targetArg = null;
        for (Argument<JDAInteractionContext> arg : command.getArgs()) {
            if (arg.name().equals(focusedOptionName)) {
                targetArg = arg;
                break;
            }
        }
        if (targetArg == null) {
            for (Argument<JDAInteractionContext> arg : command.getOptionalArgs()) {
                if (arg.name().equals(focusedOptionName)) {
                    targetArg = arg;
                    break;
                }
            }
        }

        if (targetArg == null) {
            event.replyChoices(List.of()).queue();
            return;
        }

        // Get the TabCompleter
        TabCompleter<JDAInteractionContext> completer = getTabCompleter(targetArg);

        if (completer == null) {
            event.replyChoices(List.of()).queue();
            return;
        }

        // Invoke the completer
        try {
            String currentInput = event.getFocusedOption().getValue();
            List<String> suggestions = completer.onCompletion(context,
                    List.of(currentInput));

            // Convert to Discord choices (max 25)
            List<Choice> choices = suggestions.stream()
                    .limit(25)
                    .map(s -> new Choice(s, s))
                    .toList();

            event.replyChoices(choices).queue();

        } catch (Exception e) {
            commandManager.getLogger().error("Error during autocomplete: " + e.getMessage());
            event.replyChoices(List.of()).queue();
        }
    }

    /**
     * Get the TabCompleter for an argument (custom or general).
     */
    private TabCompleter<JDAInteractionContext> getTabCompleter(Argument<JDAInteractionContext> arg) {
        // Check for custom TabCompleter on the argument
        if (arg.tabCompleter() != null) {
            return arg.tabCompleter();
        }

        // Check for general TabCompleter for this type
        return commandManager.getTabCompleterForType(arg.type().key());
    }

    private String buildLabel(SlashCommandInteractionEvent event) {
        return buildLabel(event.getName(), event.getSubcommandGroup(), event.getSubcommandName());
    }

    private String buildLabel(CommandAutoCompleteInteractionEvent event) {
        return buildLabel(event.getName(), event.getSubcommandGroup(), event.getSubcommandName());
    }

    @NotNull
    private String buildLabel(String name, String subcommandGroup, String subcommandName) {
        StringBuilder label = new StringBuilder(name);
        if (subcommandGroup != null) {
            label.append(".").append(subcommandGroup);
        }
        if (subcommandName != null) {
            label.append(".").append(subcommandName);
        }
        return label.toString().toLowerCase();
    }
}