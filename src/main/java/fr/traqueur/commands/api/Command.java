package fr.traqueur.commands.api;

import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.TabConverter;
import fr.traqueur.commands.api.exceptions.NoOptionalArgsWithInfiniteArgumentException;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Cette classe abstraite représente une commande du plugin.
 * Elle permet de créer et gérer des commandes avec leurs sous-commandes, des arguments et des permissions.
 *
 */
public abstract class Command {

    // Attributs de la classe

    /**
     * Le plugin auquel appartient la commande.
     */
    private final JavaPlugin plugin;

    /**
     * Le nom de la commande.
     */
    private final String name;

    /**
     * La liste des alias de la commande.
     */
    private final ArrayList<String> aliases;

    /**
     * La liste des sous-commandes de la commande.
     */
    private final ArrayList<Command> subcommands;

    /**
     * La liste des arguments de la commande.
     */
    private final ArrayList<Argument> args;

    /**
     * La liste des arguments optionnels de la commande.
     */
    private final ArrayList<Argument> optionalArgs;

    /**
     * La description de la commande.
     */
    private String description;

    /**
     * L'utilisation de la commande.
     */
    private String usage;

    /**
     * La permission requise pour exécuter la commande.
     */
    private String permission;

    /**
     * Indique si la commande est uniquement exécutable en jeu.
     */
    private boolean gameOnly;

    /**
     * Indique si la commande prend un nombre infini d'arguments.
     */
    private boolean infiniteArgs;

    /**
     * Constructeur de la classe Command.
     *
     * @param plugin Le plugin auquel appartient la commande.
     * @param name   Le nom de la commande.
     */
    public Command(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.permission = "";
        this.usage = "";
        this.description = "";
        this.gameOnly = false;
        this.aliases = new ArrayList<>();
        this.subcommands = new ArrayList<>();
        this.args = new ArrayList<>();
        this.optionalArgs = new ArrayList<>();
    }

    // Méthodes abstraites

    /**
     * Méthode abstraite à implémenter pour exécuter la commande.
     *
     * @param sender L'expéditeur de la commande.
     * @param args   Les arguments de la commande.
     */
    public abstract void execute(CommandSender sender, Arguments args);

    // Getters pour les attributs

    /**
     * Retourne le nom de la commande.
     *
     * @return Le nom de la commande.
     */
    public String getName() {
        return name;
    }

    /**
     * Retourne la description de la commande.
     *
     * @return La description de la commande.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retourne la permission requise pour exécuter la commande.
     *
     * @return La permission requise pour exécuter la commande.
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Retourne l'utilisation de la commande.
     *
     * @return L'utilisation de la commande.
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Retourne la liste des alias de la commande.
     *
     * @return La liste des alias de la commande.
     */
    public ArrayList<String> getAliases() {
        return aliases;
    }

    /**
     * Retourne la liste des sous-commandes de la commande.
     *
     * @return La liste des sous-commandes de la commande.
     */
    public ArrayList<Command> getSubcommands() {
        return subcommands;
    }

    /**
     * Retourne la liste des arguments de la commande.
     *
     * @return La liste des arguments de la commande.
     */
    public ArrayList<Argument> getArgs() {
        return args;
    }

    /**
     * Retourne la liste des arguments optionnels de la commande.
     *
     * @return La liste des arguments optionnels de la commande.
     */
    public ArrayList<Argument> getOptinalArgs() {
        return optionalArgs;
    }

    /**
     * Retourne true si la commande est uniquement exécutable en jeu, sinon false.
     *
     * @return true si la commande est uniquement exécutable en jeu, sinon false.
     */
    public boolean inGameOnly() {
        return gameOnly;
    }

    /**
     * Retourne true si la commande prend un nombre infini d'arguments, sinon false.
     *
     * @return true si la commande prend un nombre infini d'arguments, sinon false.
     */
    public boolean isInfiniteArgs() {
        return infiniteArgs;
    }

    // Setters pour les attributs

    /**
     * Définit la description de la commande.
     *
     * @param description La description de la commande.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Définit si la commande est uniquement exécutable en jeu.
     *
     * @param gameOnly true si la commande est uniquement exécutable en jeu, sinon false.
     */
    public void setGameOnly(boolean gameOnly) {
        this.gameOnly = gameOnly;
    }

    /**
     * Définit la permission requise pour exécuter la commande.
     *
     * @param permission La permission requise pour exécuter la commande.
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * Définit l'utilisation de la commande.
     *
     * @param usage L'utilisation de la commande.
     */
    public void setUsage(String usage) {
        this.usage = usage;
    }

    /**
     * Ajoute des alias à la commande.
     * @param aliases Les alias à ajouter.
     */
    public void addAlias(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
    }

    /**
     * Ajoute un alias à la commande.
     * @param alias L'alias à ajouter.
     */
    public void addAlias(String alias) {
        this.aliases.add(alias);
    }

    /**
     * Ajoute des sous-commandes à la commande.
     * @param commands Les sous-commandes à ajouter.
     */
    public void addSubCommand(Command... commands) {
        this.subcommands.addAll(Arrays.asList(commands));
    }

    /**
     * Ajoute une sous-commande à la commande.
     * @param command La sous-commande à ajouter.
     */
    public void addSubCommand(Command command) {
        this.subcommands.add(command);
    }

    /**
     * Ajoute des arguments à la commande.
     * @param args Les arguments à ajouter.
     */
    public void addArgs(String... args) {
        Arrays.asList(args).forEach(this::addArgs);
    }


    /**
     * Ajoute un argument à la commande.
     * @param arg L'argument à ajouter.
     */
    public void addArgs(String arg) {
        this.addArgs(arg, () -> null);
    }

    /**
     * Ajoute un argument à la commande.
     * @param arg L'argument à ajouter.
     */
    public void addArgs(String arg, TabConverter converter) {
        if (arg.contains(":infinite")) {
            this.infiniteArgs = true;
        }
        this.args.add(new Argument(arg, converter.onCompletion()));
    }

    /**
     * Ajoute des arguments optionnels à la commande.
     * @param args Les arguments optionnels à ajouter.
     */
    public void addOptinalArgs(String... args) {
        for (String a : args) {
            this.addOptinalArgs(a);
        }
    }

    /**
     * Ajoute un argument optionnel à la commande.
     * @param arg L'argument optionnel à ajouter.
     */
    public void addOptinalArgs(String arg) {
        this.addOptinalArgs(arg, () -> null);
    }

    /**
     * Ajoute un argument optionnel à la commande.
     * @param arg L'argument optionnel à ajouter.
     */
    public void addOptinalArgs(String arg, TabConverter converter) {
        try {
            if (this.infiniteArgs) {
                throw new NoOptionalArgsWithInfiniteArgumentException();
            }
            this.optionalArgs.add(new Argument(arg, converter.onCompletion()));
        } catch (NoOptionalArgsWithInfiniteArgumentException e) {
            e.printStackTrace();
        }
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}
