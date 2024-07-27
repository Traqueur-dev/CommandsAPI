package fr.traqueur.commands.api.arguments;

/**
 * The class Argument.
 * <p> This class is used to represent an argument of a command. </p>
 * @param arg The argument.
 * @param tabConverter The tab converter of the argument.
 */
public record Argument(String arg, TabConverter tabConverter) {}
