package fr.traqueur.commands.api.parsing;

import fr.traqueur.commands.api.arguments.Arguments;

public record ParseResult(
        Arguments arguments,
        ParseError error,
        int consumedCount
) {

    /**
     * Create a successful result.
     */
    public static ParseResult success(Arguments args, int consumed) {
        return new ParseResult(args, null, consumed);
    }

    /**
     * Create an error result.
     */
    public static ParseResult error(ParseError error) {
        return new ParseResult(null, error, 0);
    }

    public boolean isSuccess() {
        return error == null && arguments != null;
    }

    public boolean isError() {
        return error != null;
    }


}
