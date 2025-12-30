package fr.traqueur.commands.api.parsing;

public record ParseError(Type type,
                         String argumentName,
                         String input,
                         String message) {

    public static ParseError typeNotFound(String argName, String typeKey) {
        return new ParseError(Type.TYPE_NOT_FOUND, argName, typeKey,
                "Unknown argument type: " + typeKey);
    }

    public static ParseError conversionFailed(String argName, String input) {
        return new ParseError(Type.CONVERSION_FAILED, argName, input,
                "Failed to convert '" + input + "' for argument '" + argName + "'");
    }

    public static ParseError tooLong(String argName) {
        return new ParseError(Type.ARGUMENT_TOO_LONG, argName, null,
                "Argument '" + argName + "' exceeds maximum length");
    }

    public static ParseError missingRequired(String argName) {
        return new ParseError(Type.MISSING_REQUIRED, argName, null,
                "Missing required argument: " + argName);
    }

    public enum Type {
        TYPE_NOT_FOUND,
        CONVERSION_FAILED,
        ARGUMENT_TOO_LONG,
        MISSING_REQUIRED,
        INVALID_FORMAT
    }
}
