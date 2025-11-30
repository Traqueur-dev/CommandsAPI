package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.collections.CommandTree;
import fr.traqueur.commands.api.requirements.Requirement;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * JDA executor that handles slash command events.
 * This class listens for SlashCommandInteractionEvent and routes them to the appropriate command.
 *
 * @param <T> The type of the bot instance.
 */
public class JDAExecutor<T> extends ListenerAdapter {

    /**
     * The command manager.
     */
    private final CommandManager<T, SlashCommandInteractionEvent> commandManager;

    /**
     * Constructor for JDAExecutor.
     *
     * @param commandManager The command manager.
     */
    public JDAExecutor(CommandManager<T, SlashCommandInteractionEvent> commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String label = buildLabel(event);
        logDebugIfEnabled(label);

        Optional<Command<T, SlashCommandInteractionEvent>> commandOpt = findCommand(event, label);
        if (commandOpt.isEmpty()) {
            return;
        }

        Command<T, SlashCommandInteractionEvent> command = commandOpt.get();

        if (!validateCommand(event, command)) {
            return;
        }

        JDAArguments jdaArguments = createArguments(event);
        executeCommand(event, command, jdaArguments, label);
    }

    /**
     * Log debug message if debug mode is enabled.
     *
     * @param label The command label.
     */
    private void logDebugIfEnabled(String label) {
        if (commandManager.isDebug()) {
            commandManager.getLogger().info("Received slash command: " + label);
        }
    }

    /**
     * Find the command in the tree.
     *
     * @param event The slash command event.
     * @param label The command label.
     * @return The command if found, empty otherwise.
     */
    private Optional<Command<T, SlashCommandInteractionEvent>> findCommand(
            SlashCommandInteractionEvent event, String label) {
        String[] labelParts = label.split("\\.");
        Optional<CommandTree.MatchResult<T, SlashCommandInteractionEvent>> found =
                commandManager.getCommands().findNode(labelParts);

        if (found.isEmpty()) {
            event.reply("Command not found!").setEphemeral(true).queue();
            return Optional.empty();
        }

        CommandTree.MatchResult<T, SlashCommandInteractionEvent> result = found.get();
        CommandTree.CommandNode<T, SlashCommandInteractionEvent> node = result.node;
        Optional<Command<T, SlashCommandInteractionEvent>> cmdOpt = node.getCommand();

        if (cmdOpt.isEmpty()) {
            event.reply("Command implementation not found!").setEphemeral(true).queue();
            return Optional.empty();
        }

        return cmdOpt;
    }

    /**
     * Validate command execution conditions (game-only, permissions, requirements).
     *
     * @param event   The slash command event.
     * @param command The command to validate.
     * @return true if validation passed, false otherwise.
     */
    private boolean validateCommand(SlashCommandInteractionEvent event,
                                     Command<T, SlashCommandInteractionEvent> command) {
        if (!checkGameOnly(event, command)) {
            return false;
        }

        if (!checkPermissions(event, command)) {
            return false;
        }

        return checkRequirements(event, command);
    }

    /**
     * Check if command is game-only (guild-only in Discord context).
     *
     * @param event   The slash command event.
     * @param command The command to check.
     * @return true if check passed, false otherwise.
     */
    private boolean checkGameOnly(SlashCommandInteractionEvent event,
                                   Command<T, SlashCommandInteractionEvent> command) {
        if (command.inGameOnly() && !event.isFromGuild()) {
            event.reply(commandManager.getMessageHandler().getOnlyInGameMessage())
                    .setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    /**
     * Check command permissions.
     *
     * @param event   The slash command event.
     * @param command The command to check.
     * @return true if check passed, false otherwise.
     */
    private boolean checkPermissions(SlashCommandInteractionEvent event,
                                      Command<T, SlashCommandInteractionEvent> command) {
        String perm = command.getPermission();
        if (!perm.isEmpty() && !commandManager.getPlatform().hasPermission(event, perm)) {
            event.reply(commandManager.getMessageHandler().getNoPermissionMessage())
                    .setEphemeral(true).queue();
            return false;
        }
        return true;
    }

    /**
     * Check command requirements.
     *
     * @param event   The slash command event.
     * @param command The command to check.
     * @return true if all requirements passed, false otherwise.
     */
    private boolean checkRequirements(SlashCommandInteractionEvent event,
                                       Command<T, SlashCommandInteractionEvent> command) {
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

    /**
     * Create JDAArguments from event options.
     *
     * @param event The slash command event.
     * @return The populated JDAArguments.
     */
    private JDAArguments createArguments(SlashCommandInteractionEvent event) {
        JDAArguments jdaArguments = new JDAArguments(commandManager.getLogger(), event);
        List<OptionMapping> options = event.getOptions();

        for (OptionMapping option : options) {
            populateArgument(jdaArguments, option);
        }

        return jdaArguments;
    }

    /**
     * Populate a single argument based on option type.
     *
     * @param arguments The arguments container.
     * @param option    The option to populate from.
     */
    private void populateArgument(JDAArguments arguments, OptionMapping option) {
        String name = option.getName();

        switch (option.getType()) {
            case STRING:
                arguments.add(name, String.class, option.getAsString());
                break;
            case INTEGER:
                arguments.add(name, Long.class, option.getAsLong());
                break;
            case NUMBER:
                arguments.add(name, Double.class, option.getAsDouble());
                break;
            case BOOLEAN:
                arguments.add(name, Boolean.class, option.getAsBoolean());
                break;
            case USER:
                arguments.add(name, User.class, option.getAsUser());
                if (option.getAsMember() != null) {
                    arguments.add(name, Member.class, option.getAsMember());
                }
                break;
            case ROLE:
                arguments.add(name, Role.class, option.getAsRole());
                break;
            case CHANNEL:
                arguments.add(name, GuildChannelUnion.class, option.getAsChannel());
                break;
            case MENTIONABLE:
                arguments.add(name, IMentionable.class, option.getAsMentionable());
                break;
            case ATTACHMENT:
                arguments.add(name, Message.Attachment.class, option.getAsAttachment());
                break;
            default:
                break;
        }
    }

    /**
     * Execute the command with error handling.
     *
     * @param event     The slash command event.
     * @param command   The command to execute.
     * @param arguments The command arguments.
     * @param label     The command label for logging.
     */
    private void executeCommand(SlashCommandInteractionEvent event,
                                 Command<T, SlashCommandInteractionEvent> command,
                                 JDAArguments arguments, String label) {
        try {
            command.execute(event, arguments);
        } catch (Exception e) {
            handleCommandError(event, label, e);
        }
    }

    /**
     * Handle command execution errors.
     *
     * @param event The slash command event.
     * @param label The command label.
     * @param e     The exception that occurred.
     */
    private void handleCommandError(SlashCommandInteractionEvent event, String label, Exception e) {
        commandManager.getLogger().error("Error executing command " + label + ": " + e.getMessage());
        e.printStackTrace();
        if (!event.isAcknowledged()) {
            event.reply("An error occurred while executing this command!")
                    .setEphemeral(true).queue();
        }
    }

    /**
     * Build a label from a slash command event.
     * Examples:
     * - /ping -> "ping"
     * - /math add -> "math.add"
     * - /admin users kick -> "admin.users.kick"
     *
     * @param event The slash command event.
     * @return The constructed label.
     */
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