package fr.traqueur.commands.impl.parsing;

import fr.traqueur.commands.api.arguments.*;
import fr.traqueur.commands.api.logging.Logger;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.parsing.ArgumentParser;
import fr.traqueur.commands.api.parsing.ParseError;
import fr.traqueur.commands.api.parsing.ParseResult;

import java.util.List;
import java.util.Map;

/**
 * Default parser for text-based commands (Spigot, Velocity).
 * Parses String[] arguments using registered converters.
 */
public class DefaultArgumentParser<T, S> implements ArgumentParser<T, S, String[]> {
    
    private static final int MAX_INFINITE_LENGTH = 10_000;
    
    private final Map<String, ArgumentConverter.Wrapper<?>> typeConverters;
    private final Logger logger;
    
    public DefaultArgumentParser(Map<String, ArgumentConverter.Wrapper<?>> typeConverters, Logger logger) {
        this.typeConverters = typeConverters;
        this.logger = logger;
    }
    
    @Override
    public ParseResult parse(Command<T, S> command, String[] rawArgs) {
        Arguments arguments = new Arguments(logger);
        
        List<Argument<S>> required = command.getArgs();
        List<Argument<S>> optional = command.getOptionalArgs();
        
        int argIndex = 0;
        
        // Parse required arguments
        for (Argument<S> arg : required) {
            if (arg.isInfinite()) {
                return parseInfinite(arguments, arg, rawArgs, argIndex);
            }
            
            if (argIndex >= rawArgs.length) {
                return ParseResult.error(new ParseError(
                    ParseError.Type.MISSING_REQUIRED,
                    arg.name(), 
                    null, 
                    "Missing required argument: " + arg.name()
                ));
            }
            
            ParseResult result = parseSingle(arguments, arg, rawArgs[argIndex]);
            if (result.isError()) {
                return result;
            }
            argIndex++;
        }
        
        // Parse optional arguments
        for (Argument<S> arg : optional) {
            if (argIndex >= rawArgs.length) {
                break;
            }
            
            if (arg.isInfinite()) {
                return parseInfinite(arguments, arg, rawArgs, argIndex);
            }
            
            ParseResult result = parseSingle(arguments, arg, rawArgs[argIndex]);
            if (result.isError()) {
                return result;
            }
            argIndex++;
        }
        
        return ParseResult.success(arguments, argIndex);
    }
    
    private ParseResult parseSingle(Arguments arguments, Argument<S> arg, String input) {
        String typeKey = arg.type().key();
        ArgumentConverter.Wrapper<?> wrapper = typeConverters.get(typeKey);
        
        if (wrapper == null) {
            return ParseResult.error(new ParseError(
                ParseError.Type.TYPE_NOT_FOUND,
                arg.name(),
                input,
                "No converter for type: " + typeKey
            ));
        }
        
        if (!wrapper.convertAndApply(input, arg.name(), arguments)) {
            return ParseResult.error(new ParseError(
                ParseError.Type.CONVERSION_FAILED,
                arg.name(),
                input,
                "Failed to convert: " + input
            ));
        }
        
        return ParseResult.success(arguments, 1);
    }
    
    private ParseResult parseInfinite(Arguments arguments, Argument<S> arg, String[] rawArgs, int startIndex) {
        if (startIndex >= rawArgs.length) {
            arguments.add(arg.name(), String.class, "");
            return ParseResult.success(arguments, 0);
        }
        
        StringBuilder sb = new StringBuilder();
        int count = 0;
        
        for (int i = startIndex; i < rawArgs.length; i++) {
            if (sb.length() > MAX_INFINITE_LENGTH) {
                return ParseResult.error(new ParseError(
                    ParseError.Type.ARGUMENT_TOO_LONG,
                    arg.name(),
                    null,
                    "Infinite argument exceeds max length"
                ));
            }
            if (i > startIndex) {
                sb.append(" ");
            }
            sb.append(rawArgs[i]);
            count++;
        }
        
        arguments.add(arg.name(), String.class, sb.toString());
        return ParseResult.success(arguments, count);
    }
}