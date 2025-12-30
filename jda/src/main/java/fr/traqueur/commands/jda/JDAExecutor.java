package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.api.parsing.ParseResult;
import fr.traqueur.commands.api.requirements.Requirement;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * JDA executor that handles slash command events.
 */
public class JDAExecutor<T> extends ListenerAdapter {

    private final CommandManager<T, SlashCommandInteractionEvent> commandManager;
    private final JDAArgumentParser<T> parser;

    public JDAExecutor(CommandManager<T, SlashCommandInteractionEvent> commandManager) {
        this.commandManager = commandManager;
        this.parser = new JDAArgumentParser<>(commandManager.getLogger());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String label = buildLabel(event);

        if (commandManager.isDebug()) {
            commandManager.getLogger().info("Received slash command: " + label);
        }

        // Find command
        String[] labelParts = label.split("\\.");
        Optional<CommandTree.MatchResult<T, SlashCommandInteractionEvent>> found =
                commandManager.getCommands().findNode(labelParts);

        if (found.isEmpty()) {
            event.reply("Command not found!").setEphemeral(true).queue();
            return;
        }

        Command<T, SlashCommandInteractionEvent> command = found.get().node().getCommand().orElse(null);
        if (command == null) {
            event.reply("Command implementation not found!").setEphemeral(true).queue();
            return;
        }

        // Validate
        if (!validateCommand(event, command)) {
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
            command.execute(event, result.arguments());
        } catch (Exception e) {
            commandManager.getLogger().error("Error executing command " + label + ": " + e.getMessage());
            if (!event.isAcknowledged()) {
                event.reply("An error occurred!").setEphemeral(true).queue();
            }
        }
    }

    private boolean validateCommand(SlashCommandInteractionEvent event,
                                    Command<T, SlashCommandInteractionEvent> command) {
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
        if (!perm.isEmpty() && !commandManager.getPlatform().hasPermission(event, perm)) {
            event.reply(commandManager.getMessageHandler().getNoPermissionMessage())
                    .setEphemeral(true).queue();
            return false;
        }

        // Requirements check
        for (Requirement<SlashCommandInteractionEvent> req : command.getRequirements()) {
            if (!req.check(event)) {
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

    private String buildLabel(SlashCommandInteractionEvent event) {
        StringBuilder label = new StringBuilder(event.getName());
        if (event.getSubcommandGroup() != null) {
            label.append(".").append(event.getSubcommandGroup());
        }
        if (event.getSubcommandName() != null) {
            label.append(".").append(event.getSubcommandName());
        }
        return label.toString().toLowerCase();
    }
}