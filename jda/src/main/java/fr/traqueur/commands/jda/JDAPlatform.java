package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandPlatform;
import fr.traqueur.commands.api.resolver.SenderResolver;
import fr.traqueur.commands.api.utils.Patterns;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;

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
public class JDAPlatform<T> implements CommandPlatform<T, JDAInteractionContext> {

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
     * Map of root command names to their SlashCommandData.
     */
    private final Map<String, SlashCommandData> slashCommands;
    /**
     * The command manager.
     */
    private CommandManager<T, JDAInteractionContext> commandManager;

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
    public void injectManager(CommandManager<T, JDAInteractionContext> commandManager) {
        this.commandManager = commandManager;
        this.jda.addEventListener(new JDAExecutor<>(commandManager));
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public boolean hasPermission(JDAInteractionContext sender, String permission) {
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
    public boolean isPlayer(JDAInteractionContext sender) {
        // In Discord context, we consider guild-only commands
        return sender.isFromGuild();
    }

    @Override
    public void sendMessage(JDAInteractionContext sender, String message) {
        if (sender.getEvent() instanceof SlashCommandInteractionEvent event) {
            if (!event.isAcknowledged()) {
                event.reply(message).queue();
            } else {
                event.getHook().sendMessage(message).queue();
            }
        }
    }

    @Override
    public void addCommand(Command<T, JDAInteractionContext> command, String label) {
        String[] parts = Patterns.DOT.split(label);
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

        } else {
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
        String[] parts = Patterns.DOT.split(label);
        String rootName = parts[0].toLowerCase();

        if (!subcommand && parts.length == 1) {
            slashCommands.remove(rootName);
        }
    }

    @Override
    public SenderResolver<JDAInteractionContext> getSenderResolver() {
        return new JDASenderResolver();
    }

    /**
     * Add arguments to a slash command.
     *
     * @param slashCommand The slash command data.
     * @param command      The command instance.
     */
    private void addArgumentsToCommand(SlashCommandData slashCommand, Command<T, JDAInteractionContext> command) {
        List<Argument<JDAInteractionContext>> args = command.getArgs();
        List<Argument<JDAInteractionContext>> optionalArgs = command.getOptionalArgs();

        for (Argument<JDAInteractionContext> arg : args) {
            OptionData option = createOptionData(arg, true);
            slashCommand.addOptions(option);
        }

        for (Argument<JDAInteractionContext> arg : optionalArgs) {
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
    private void addArgumentsToSubcommand(SubcommandData subcommand, Command<T, JDAInteractionContext> command) {
        List<Argument<JDAInteractionContext>> args = command.getArgs();
        List<Argument<JDAInteractionContext>> optionalArgs = command.getOptionalArgs();

        for (Argument<JDAInteractionContext> arg : args) {
            OptionData option = createOptionData(arg, true);
            subcommand.addOptions(option);
        }

        for (Argument<JDAInteractionContext> arg : optionalArgs) {
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
    private OptionData createOptionData(Argument<JDAInteractionContext> arg, boolean required) {
        String name = arg.name();

        OptionType optionType = mapToOptionType(arg.type().key());

        OptionData optionData = new OptionData(optionType, name, "Argument: " + name, required);

        // Enable autocomplete if:
        // 1. The argument has a custom TabCompleter, OR
        // 2. A general TabCompleter exists for this type
        if (hasTabCompleter(arg)) {
            optionData.setAutoComplete(true);
        }

        return optionData;
    }

    /**
     * Check if an argument has a TabCompleter (either custom or general).
     *
     * @param arg The argument to check.
     * @return true if a TabCompleter exists for this argument.
     */
    private boolean hasTabCompleter(Argument<JDAInteractionContext> arg) {
        // Check for custom TabCompleter on the argument itself
        if (arg.tabCompleter() != null) {
            return true;
        }

        // Check for general TabCompleter registered for this type
        if (commandManager != null) {
            return commandManager.hasTabCompleterForType(arg.type().key());
        }

        return false;
    }

    /**
     * Map a type string to a JDA OptionType.
     *
     * @param type The type string.
     * @return The corresponding OptionType.
     */
    private OptionType mapToOptionType(Class<?> type) {
        return switch (type) {

            // INTEGER
            case Class<?> t when t == int.class
                    || t == Integer.class
                    || t == long.class
                    || t == Long.class
                    -> OptionType.INTEGER;

            // BOOLEAN
            case Class<?> t when t == boolean.class
                    || t == Boolean.class
                    -> OptionType.BOOLEAN;

            // USER / MEMBER
            case Class<?> t when User.class.isAssignableFrom(t)
                    || Member.class.isAssignableFrom(t)
                    -> OptionType.USER;

            // ROLE
            case Class<?> t when Role.class.isAssignableFrom(t)
                    -> OptionType.ROLE;

            // CHANNEL (tous types de channels)
            case Class<?> t when GuildChannel.class.isAssignableFrom(t)
                    -> OptionType.CHANNEL;

            // NUMBER
            case Class<?> t when t == double.class
                    || t == Double.class
                    || t == float.class
                    || t == Float.class
                    -> OptionType.NUMBER;

            // ATTACHMENT
            case Class<?> t when Message.Attachment.class.isAssignableFrom(t)
                    -> OptionType.ATTACHMENT;

            // MENTIONABLE
            case Class<?> t when IMentionable.class.isAssignableFrom(t)
                    -> OptionType.MENTIONABLE;

            default -> OptionType.STRING;
        };
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
    public CommandManager<T, JDAInteractionContext> getCommandManager() {
        return commandManager;
    }
}