package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandPlatform;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * JDA implementation of the CommandPlatform interface.
 * This class handles the registration and management of slash commands in Discord.
 *
 * @param <T> The type of the bot instance.
 */
public class JDAPlatform<T> implements CommandPlatform<T, SlashCommandInteractionEvent> {

    /**
     * The bot instance associated with this platform.
     */
    private final T bot;

    /**
     * The JDA instance.
     */
    private final JDA jda;

    /**
     * The logger for this platform.
     */
    private final Logger logger;

    /**
     * The command manager.
     */
    private CommandManager<T, SlashCommandInteractionEvent> commandManager;

    /**
     * The executor that handles slash command events.
     */
    private JDAExecutor<T> executor;

    /**
     * Map of root command names to their SlashCommandData.
     */
    private final Map<String, SlashCommandData> slashCommands;

    /**
     * Constructor for JDAPlatform.
     *
     * @param bot    The bot instance.
     * @param jda    The JDA instance.
     * @param logger The logger instance.
     */
    public JDAPlatform(T bot, JDA jda, Logger logger) {
        this.bot = bot;
        this.jda = jda;
        this.logger = logger;
        this.slashCommands = new HashMap<>();
    }

    @Override
    public T getPlugin() {
        return bot;
    }

    @Override
    public void injectManager(CommandManager<T, SlashCommandInteractionEvent> commandManager) {
        this.commandManager = commandManager;
        this.executor = new JDAExecutor<>(bot, commandManager);
        this.jda.addEventListener(executor);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public boolean hasPermission(SlashCommandInteractionEvent sender, String permission) {
        if (sender.getMember() == null) {
            return false;
        }
        try {
            Permission perm = Permission.valueOf(permission.toUpperCase());
            return sender.getMember().hasPermission(perm);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid permission: " + permission);
            return false;
        }
    }

    @Override
    public boolean isPlayer(SlashCommandInteractionEvent sender) {
        // In Discord context, we consider guild-only commands
        return sender.isFromGuild();
    }

    @Override
    public void sendMessage(SlashCommandInteractionEvent sender, String message) {
        if (!sender.isAcknowledged()) {
            sender.reply(message).queue();
        } else {
            sender.getHook().sendMessage(message).queue();
        }
    }

    @Override
    public void addCommand(Command<T, SlashCommandInteractionEvent> command, String label) {
        String[] parts = label.split("\\.");
        String rootName = parts[0].toLowerCase();

        if (parts.length == 1) {
            // Root command
            SlashCommandData slashCommand = Commands.slash(rootName,
                    command.getDescription().isEmpty() ? "No description" : command.getDescription());

            // Add arguments if this is a leaf command
            if (command.getSubcommands().isEmpty()) {
                addArgumentsToCommand(slashCommand, command);
            }

            slashCommands.put(rootName, slashCommand);

        } else if (parts.length == 2) {
            // Subcommand: /command subcommand
            // Skip if this command has subcommands (it will be treated as a group)
            if (!command.getSubcommands().isEmpty()) {
                // This is a group, not a leaf subcommand - skip it
                // Its children will be registered as subcommand groups (parts.length >= 3)
                logger.warning(String.format(
                    "Command '%s' has subcommands and will be treated as a subcommand group. " +
                    "Discord does not support executing intermediate groups. " +
                    "If you want this group to be executable, create a dedicated subcommand (e.g., '%s.list' or '%s.info').",
                    label, label, label
                ));
                return;
            }

            String subName = parts[1].toLowerCase();

            SlashCommandData slashCommand = slashCommands.computeIfAbsent(rootName,
                    k -> Commands.slash(rootName, "Command group"));

            SubcommandData subcommand = new SubcommandData(subName,
                    command.getDescription().isEmpty() ? "No description" : command.getDescription());

            addArgumentsToSubcommand(subcommand, command);
            slashCommand.addSubcommands(subcommand);

        } else if (parts.length >= 3) {
            // Subcommand group: /command group subcommand
            String groupName = parts[1].toLowerCase();
            String subName = parts[2].toLowerCase();

            SlashCommandData slashCommand = slashCommands.computeIfAbsent(rootName,
                    k -> Commands.slash(rootName, "Command group"));

            // Find or create the subcommand group
            SubcommandGroupData group = null;
            for (var existingGroup : slashCommand.getSubcommandGroups()) {
                if (existingGroup.getName().equals(groupName)) {
                    group = existingGroup;
                    break;
                }
            }

            if (group == null) {
                group = new SubcommandGroupData(groupName, "Subcommand group");
                slashCommand.addSubcommandGroups(group);
            }

            SubcommandData subcommand = new SubcommandData(subName,
                    command.getDescription().isEmpty() ? "No description" : command.getDescription());

            addArgumentsToSubcommand(subcommand, command);
            group.addSubcommands(subcommand);
        }
    }

    @Override
    public void removeCommand(String label, boolean subcommand) {
        String[] parts = label.split("\\.");
        String rootName = parts[0].toLowerCase();

        if (!subcommand && parts.length == 1) {
            slashCommands.remove(rootName);
        }
    }

    /**
     * Add arguments to a slash command.
     *
     * @param slashCommand The slash command data.
     * @param command      The command instance.
     */
    private void addArgumentsToCommand(SlashCommandData slashCommand, Command<T, SlashCommandInteractionEvent> command) {
        List<Argument<SlashCommandInteractionEvent>> args = command.getArgs();
        List<Argument<SlashCommandInteractionEvent>> optionalArgs = command.getOptinalArgs();

        for (Argument<SlashCommandInteractionEvent> arg : args) {
            OptionData option = createOptionData(arg, true);
            slashCommand.addOptions(option);
        }

        for (Argument<SlashCommandInteractionEvent> arg : optionalArgs) {
            OptionData option = createOptionData(arg, false);
            slashCommand.addOptions(option);
        }
    }

    /**
     * Add arguments to a subcommand.
     *
     * @param subcommand The subcommand data.
     * @param command    The command instance.
     */
    private void addArgumentsToSubcommand(SubcommandData subcommand, Command<T, SlashCommandInteractionEvent> command) {
        List<Argument<SlashCommandInteractionEvent>> args = command.getArgs();
        List<Argument<SlashCommandInteractionEvent>> optionalArgs = command.getOptinalArgs();

        for (Argument<SlashCommandInteractionEvent> arg : args) {
            OptionData option = createOptionData(arg, true);
            subcommand.addOptions(option);
        }

        for (Argument<SlashCommandInteractionEvent> arg : optionalArgs) {
            OptionData option = createOptionData(arg, false);
            subcommand.addOptions(option);
        }
    }

    /**
     * Create an OptionData from an Argument.
     *
     * @param arg      The argument.
     * @param required Whether the argument is required.
     * @return The OptionData.
     */
    private OptionData createOptionData(Argument<SlashCommandInteractionEvent> arg, boolean required) {
        String[] parts = arg.arg().split(CommandManager.TYPE_PARSER);
        String name = parts[0].trim().toLowerCase();
        String type = parts.length > 1 ? parts[1].trim() : "string";

        OptionType optionType = mapToOptionType(type);
        OptionData option = new OptionData(optionType, name, "Argument: " + name, required);

        return option;
    }

    /**
     * Map a type string to a JDA OptionType.
     *
     * @param type The type string.
     * @return The corresponding OptionType.
     */
    private OptionType mapToOptionType(String type) {
        switch (type.toLowerCase()) {
            case "integer":
            case "int":
            case "long":
                return OptionType.INTEGER;
            case "boolean":
                return OptionType.BOOLEAN;
            case "user":
            case "member":
                return OptionType.USER;
            case "role":
                return OptionType.ROLE;
            case "channel":
            case "guildchannelunion":
                return OptionType.CHANNEL;
            case "double":
            case "float":
                return OptionType.NUMBER;
            case "attachment":
                return OptionType.ATTACHMENT;
            case "mentionable":
                return OptionType.MENTIONABLE;
            default:
                return OptionType.STRING;
        }
    }

    /**
     * Synchronize all registered commands with Discord globally.
     * This may take up to 1 hour to update.
     */
    public void syncCommands() {
        if (commandManager.isDebug()) {
            logger.info("Syncing " + slashCommands.size() + " commands to Discord globally...");
        }
        jda.updateCommands()
                .addCommands(slashCommands.values())
                .queue(
                        success -> logger.info("Successfully synced " + success.size() + " commands globally"),
                        error -> logger.severe("Failed to sync commands: " + error.getMessage())
                );
    }

    /**
     * Synchronize all registered commands with a specific guild.
     * This updates instantly and is useful for testing.
     *
     * @param guildId The guild ID.
     */
    public void syncCommandsToGuild(long guildId) {
        if (commandManager.isDebug()) {
            logger.info("Syncing " + slashCommands.size() + " commands to guild " + guildId + "...");
        }
        jda.getGuildById(guildId).updateCommands()
                .addCommands(slashCommands.values())
                .queue(
                        success -> logger.info("Successfully synced " + success.size() + " commands to guild " + guildId),
                        error -> logger.severe("Failed to sync commands to guild: " + error.getMessage())
                );
    }

    /**
     * Synchronize all registered commands with a specific guild.
     *
     * @param guildId The guild ID as a string.
     */
    public void syncCommandsToGuild(String guildId) {
        syncCommandsToGuild(Long.parseLong(guildId));
    }

    /**
     * Get the JDA instance.
     *
     * @return The JDA instance.
     */
    public JDA getJDA() {
        return jda;
    }

    /**
     * Get the command manager.
     *
     * @return The command manager.
     */
    public CommandManager<T, SlashCommandInteractionEvent> getCommandManager() {
        return commandManager;
    }
}