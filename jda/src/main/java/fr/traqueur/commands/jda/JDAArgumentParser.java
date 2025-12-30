package fr.traqueur.commands.jda;

import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.arguments.ArgumentType;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.logging.Logger;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.parsing.ArgumentParser;
import fr.traqueur.commands.api.parsing.ParseError;
import fr.traqueur.commands.api.parsing.ParseResult;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * JDA-specific argument parser that uses Discord's OptionMapping.
 * Discord handles type resolution natively, so no string conversion needed.
 */
public record JDAArgumentParser<T>(Logger logger) implements ArgumentParser<T, SlashCommandInteractionEvent, SlashCommandInteractionEvent> {

    @Override
    public ParseResult parse(Command<T, SlashCommandInteractionEvent> command, SlashCommandInteractionEvent event) {
        JDAArguments arguments = new JDAArguments(logger, event);
        List<OptionMapping> options = event.getOptions();

        List<Argument<SlashCommandInteractionEvent>> allArgs = new ArrayList<>();
        allArgs.addAll(command.getArgs());
        allArgs.addAll(command.getOptionalArgs());

        // Validate argument count
        if (options.size() < command.getArgs().size()) {
            return ParseResult.error(new ParseError(
                    ParseError.Type.MISSING_REQUIRED,
                    null,
                    null,
                    "Not enough arguments provided"
            ));
        }

        int maxOptional = command.getOptionalArgs().size();
        int providedOptional = options.size() - command.getArgs().size();
        if (providedOptional > maxOptional) {
            return ParseResult.error(new ParseError(
                    ParseError.Type.INVALID_FORMAT,
                    null,
                    null,
                    "Too many arguments provided"
            ));
        }

        // Parse each option
        for (int i = 0; i < options.size(); i++) {
            OptionMapping option = options.get(i);
            Argument<SlashCommandInteractionEvent> arg = allArgs.get(i);

            try {
                populateArgument(arguments, option, arg);
            } catch (ArgumentIncorrectException e) {
                return ParseResult.error(new ParseError(
                        ParseError.Type.CONVERSION_FAILED,
                        option.getName(),
                        null,
                        e.getMessage()
                ));
            }
        }

        return ParseResult.success(arguments, options.size());
    }

    private void populateArgument(JDAArguments arguments, OptionMapping option,
                                  Argument<SlashCommandInteractionEvent> arg) {
        String name = option.getName();

        switch (option.getType()) {
            case STRING -> arguments.add(name, String.class, option.getAsString());

            case INTEGER -> {
                if (!(arg.type() instanceof ArgumentType.Simple(Class<?> clazz))) {
                    throw new ArgumentIncorrectException(name);
                }
                if (clazz == Integer.class || clazz == int.class) {
                    arguments.add(name, Integer.class, option.getAsInt());
                } else if (clazz == Long.class || clazz == long.class) {
                    arguments.add(name, Long.class, option.getAsLong());
                } else {
                    throw new ArgumentIncorrectException(name);
                }
            }

            case NUMBER -> {
                if (!(arg.type() instanceof ArgumentType.Simple(Class<?> clazz))) {
                    throw new ArgumentIncorrectException(name);
                }
                if (clazz == Double.class || clazz == double.class) {
                    arguments.add(name, Double.class, option.getAsDouble());
                } else if (clazz == Float.class || clazz == float.class) {
                    arguments.add(name, Float.class, (float) option.getAsDouble());
                } else {
                    throw new ArgumentIncorrectException(name);
                }
            }

            case BOOLEAN -> arguments.add(name, Boolean.class, option.getAsBoolean());

            case USER -> {
                if (!(arg.type() instanceof ArgumentType.Simple(Class<?> clazz))) {
                    throw new ArgumentIncorrectException(name);
                }
                if (clazz == Member.class) {
                    arguments.add(name, Member.class, option.getAsMember());
                } else if (clazz == User.class) {
                    arguments.add(name, User.class, option.getAsUser());
                } else {
                    throw new ArgumentIncorrectException(name);
                }
            }

            case ROLE -> arguments.add(name, Role.class, option.getAsRole());
            case CHANNEL -> arguments.add(name, GuildChannelUnion.class, option.getAsChannel());
            case MENTIONABLE -> arguments.add(name, IMentionable.class, option.getAsMentionable());
            case ATTACHMENT -> arguments.add(name, Message.Attachment.class, option.getAsAttachment());

            default -> { /* Unknown type, skip */ }
        }
    }
}