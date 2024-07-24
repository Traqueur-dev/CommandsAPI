package fr.traqueur.commands.api.arguments;

import java.util.List;

/**
 * The class Argument.
 * <p> This class is used to represent an argument of a command. </p>
 * @param arg The argument.
 * @param completion The list of completion.
 */
public record Argument(String arg, List<String> completion) {}
