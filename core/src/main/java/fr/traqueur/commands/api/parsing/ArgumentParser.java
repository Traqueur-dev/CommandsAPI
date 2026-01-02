package fr.traqueur.commands.api.parsing;

import fr.traqueur.commands.api.models.Command;

/**
 * Interface for platform-specific argument parsing.
 *
 * @param <T> plugin type
 * @param <S> sender type
 * @param <C> context type (String[] for text commands, SlashCommandInteractionEvent for JDA)
 */
public interface ArgumentParser<T, S, C> {

    /**
     * Parse arguments from the given context.
     *
     * @param command the command being executed
     * @param context the parsing context (raw args or event)
     * @return the parse result
     */
    ParseResult parse(Command<T, S> command, C context);
}