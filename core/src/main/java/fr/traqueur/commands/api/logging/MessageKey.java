package fr.traqueur.commands.api.logging;

/**
 * Identifies a message the library sends to a sender.
 * <p>
 * Each key comes with named values, passed separately from the text so that a formatter can
 * place them itself (a MiniMessage tag, a translation argument) instead of receiving them
 * already substituted:
 * </p>
 * <ul>
 *     <li>{@link #ARG_NOT_RECOGNIZED}, {@link #TOO_LONG}, {@link #INVALID_FORMAT}: {@code arg}, the input;</li>
 *     <li>{@link #REQUIREMENT}: {@code requirement}, the requirement's
 *     {@link fr.traqueur.commands.api.requirements.Requirement#errorMessage() error message}, or its
 *     {@link fr.traqueur.commands.api.requirements.Requirement#name() name} when the message is empty;</li>
 *     <li>{@link #USAGE}: {@code usage}, the usage of the command;</li>
 *     <li>the others: no value.</li>
 * </ul>
 *
 * @since 5.5.0
 */
public enum MessageKey {

    /**
     * The sender lacks the permission of the command.
     */
    NO_PERMISSION,

    /**
     * The command can only be run by a player.
     */
    ONLY_IN_GAME,

    /**
     * An argument could not be converted.
     */
    ARG_NOT_RECOGNIZED,

    /**
     * A requirement of the command is not met.
     */
    REQUIREMENT,

    /**
     * The command is disabled.
     */
    DISABLED,

    /**
     * An argument exceeds its maximum length.
     */
    TOO_LONG,

    /**
     * An argument has an invalid format.
     */
    INVALID_FORMAT,

    /**
     * The command was run with a wrong number of arguments; the message is its usage.
     */
    USAGE,

    /**
     * An argument has a type no converter is registered for: a bug of the plugin.
     */
    INTERNAL_ERROR;

    /**
     * The name of the value holding the input of an argument.
     */
    public static final String ARG = "arg";

    /**
     * The name of the value holding the reason of a requirement.
     */
    public static final String REQUIREMENT_VALUE = "requirement";

    /**
     * The name of the value holding the usage of a command.
     */
    public static final String USAGE_VALUE = "usage";
}
