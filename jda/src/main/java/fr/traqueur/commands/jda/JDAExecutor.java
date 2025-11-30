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
     * The bot instance.
     */
    private final T bot;

    /**
     * The command manager.
     */
    private final CommandManager<T, SlashCommandInteractionEvent> commandManager;

    /**
     * Constructor for JDAExecutor.
     *
     * @param bot            The bot instance.
     * @param commandManager The command manager.
     */
    public JDAExecutor(T bot, CommandManager<T, SlashCommandInteractionEvent> commandManager) {
        this.bot = bot;
        this.commandManager = commandManager;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        // Build the label from the event
        String label = buildLabel(event);

        if (commandManager.isDebug()) {
            commandManager.getLogger().info("Received slash command: " + label);
        }

        // Find the command in the tree
        String[] labelParts = label.split("\\.");
        Optional<CommandTree.MatchResult<T, SlashCommandInteractionEvent>> found =
                commandManager.getCommands().findNode(labelParts);

        if (found.isEmpty()) {
            event.reply("Command not found!").setEphemeral(true).queue();
            return;
        }

        CommandTree.MatchResult<T, SlashCommandInteractionEvent> result = found.get();
        CommandTree.CommandNode<T, SlashCommandInteractionEvent> node = result.node;
        Optional<Command<T, SlashCommandInteractionEvent>> cmdOpt = node.getCommand();

        if (cmdOpt.isEmpty()) {
            event.reply("Command implementation not found!").setEphemeral(true).queue();
            return;
        }

        Command<T, SlashCommandInteractionEvent> command = cmdOpt.get();

        // Check if command is game-only (guild-only in Discord context)
        if (command.inGameOnly() && !event.isFromGuild()) {
            event.reply(commandManager.getMessageHandler().getOnlyInGameMessage()).setEphemeral(true).queue();
            return;
        }

        // Check permissions
        String perm = command.getPermission();
        if (!perm.isEmpty() && !commandManager.getPlatform().hasPermission(event, perm)) {
            event.reply(commandManager.getMessageHandler().getNoPermissionMessage()).setEphemeral(true).queue();
            return;
        }

        // Check requirements
        for (Requirement<SlashCommandInteractionEvent> req : command.getRequirements()) {
            if (!req.check(event)) {
                String msg = req.errorMessage().isEmpty()
                        ? commandManager.getMessageHandler().getRequirementMessage()
                        .replace("%requirement%", req.getClass().getSimpleName())
                        : req.errorMessage();
                event.reply(msg).setEphemeral(true).queue();
                return;
            }
        }

        // Create JDAArguments from event options
        JDAArguments jdaArguments = new JDAArguments(commandManager.getLogger(), event);

        // Populate arguments from event options
        List<OptionMapping> options = event.getOptions();
        for (OptionMapping option : options) {
            String name = option.getName();

            switch (option.getType()) {
                case STRING:
                    jdaArguments.add(name, String.class, option.getAsString());
                    break;
                case INTEGER:
                    jdaArguments.add(name, Long.class, option.getAsLong());
                    break;
                case NUMBER:
                    jdaArguments.add(name, Double.class, option.getAsDouble());
                    break;
                case BOOLEAN:
                    jdaArguments.add(name, Boolean.class, option.getAsBoolean());
                    break;
                case USER:
                    jdaArguments.add(name, User.class, option.getAsUser());
                    if (option.getAsMember() != null) {
                        jdaArguments.add(name, Member.class, option.getAsMember());
                    }
                    break;
                case ROLE:
                    jdaArguments.add(name, Role.class, option.getAsRole());
                    break;
                case CHANNEL:
                    jdaArguments.add(name, GuildChannelUnion.class,
                            option.getAsChannel());
                    break;
                case MENTIONABLE:
                    jdaArguments.add(name, IMentionable.class, option.getAsMentionable());
                    break;
                case ATTACHMENT:
                    jdaArguments.add(name, Message.Attachment.class,
                            option.getAsAttachment());
                    break;
                default:
                    break;
            }
        }

        // Execute the command
        try {
            command.execute(event, jdaArguments);
        } catch (Exception e) {
            commandManager.getLogger().error("Error executing command " + label + ": " + e.getMessage());
            e.printStackTrace();
            if (!event.isAcknowledged()) {
                event.reply("An error occurred while executing this command!").setEphemeral(true).queue();
            }
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