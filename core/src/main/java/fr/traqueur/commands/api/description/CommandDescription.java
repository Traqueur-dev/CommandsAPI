package fr.traqueur.commands.api.description;

import java.util.List;

/**
 * A public, read-only description of a command, to generate the documentation of a plugin's
 * commands without going through the internal structures of the library.
 *
 * @param name        The name of the command, as registered.
 * @param path        The words typed to reach it, the parents of a subcommand included ({@code home set}).
 * @param aliases     Its aliases.
 * @param description Its description, empty when none is set.
 * @param usage       Its usage: the one set on the command, or the one the library generates.
 * @param permission  Its permission, empty when none is required.
 * @param inGameOnly  Whether only a player can run it.
 * @param arguments   Its arguments, the required ones first, in order.
 * @param subcommands Its subcommands.
 * @since 5.7.0
 */
public record CommandDescription(String name,
                                 String path,
                                 List<String> aliases,
                                 String description,
                                 String usage,
                                 String permission,
                                 boolean inGameOnly,
                                 List<ArgumentDescription> arguments,
                                 List<CommandDescription> subcommands) {

    /**
     * Create a description.
     */
    public CommandDescription {
        aliases = List.copyOf(aliases);
        arguments = List.copyOf(arguments);
        subcommands = List.copyOf(subcommands);
    }

    /**
     * The required arguments of the command.
     *
     * @return The required arguments, in order.
     */
    public List<ArgumentDescription> requiredArguments() {
        return this.arguments.stream().filter(argument -> !argument.optional()).toList();
    }

    /**
     * The optional arguments of the command.
     *
     * @return The optional arguments, in order.
     */
    public List<ArgumentDescription> optionalArguments() {
        return this.arguments.stream().filter(ArgumentDescription::optional).toList();
    }
}
