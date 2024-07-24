package fr.traqueur.commands.api;

import com.google.common.collect.Lists;
import fr.traqueur.commands.api.arguments.Argument;
import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.TabConverter;
import fr.traqueur.commands.api.arguments.impl.*;
import fr.traqueur.commands.api.exceptions.ArgumentIncorrectException;
import fr.traqueur.commands.api.exceptions.TemplateArgumentNotExistException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cette classe gère l'enregistrement et l'exécution des commandes personnalisées.
 */
public class CommandManager implements CommandExecutor, TabCompleter {

    private final Plugin plugin;
    private final CommandMap commandMap;
    private final Constructor<? extends PluginCommand> pluginConstructor;
    private final Map<String, Command> commands;
    private final Map<String, Map.Entry<Class<?>, ArgumentConverter<?>>> typeConverters;
    private final Map<String, Map<Integer, TabConverter>> completers;

    /**
     * Constructeur de la classe CommandManager.
     * @param plugin Le plugin utilisant ce gestionnaire de commandes.
     */
    public CommandManager(Plugin plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
        this.typeConverters = new HashMap<>();
        this.completers = new HashMap<>();
        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            pluginConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            pluginConstructor.setAccessible(true);
        } catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        this.registerConverter(String.class, "string", (s) -> s);
        this.registerConverter(Integer.class, "int",new IntegerArgument());
        this.registerConverter(Double.class, "double",new DoubleArgument());
        this.registerConverter(Long.class, "long", new LongArgument());
        this.registerConverter(Player.class, "player", new PlayerArgument());
        this.registerConverter(OfflinePlayer.class, "offlineplayer", new OfflinePlayerArgument());
        this.registerConverter(String.class, "infinite", s -> s);
    }

    /**
     * Enregistre une commande personnalisée.
     * @param command La commande à enregistrer.
     */
    public void registerCommand(Command command) {
        try {
            ArrayList<String> aliases = new ArrayList<>(command.getAliases());
            aliases.add(command.getName());
            for (String alias : aliases) {
                this.registerCommand(command, alias);
                this.registerSubCommands(alias, command.getSubcommands());
            }
        } catch(TemplateArgumentNotExistException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Enregistre les sous-commandes d'une commande.
     * @param parentLabel Le nom de la commande parente.
     * @param subcommands Les sous-commandes à enregistrer.
     * @throws TemplateArgumentNotExistException Si le modèle d'argument n'existe pas.
     */
    public void registerSubCommands(String parentLabel, List<Command> subcommands) throws TemplateArgumentNotExistException {
        if(subcommands == null || subcommands.isEmpty()) {
            return;
        }
        for (Command subcommand : subcommands) {
            ArrayList<String> aliasesSub = new ArrayList<>(subcommand.getAliases());
            aliasesSub.add(subcommand.getName());
            for (String aliasSub : aliasesSub) {
                this.registerCommand(subcommand, parentLabel + "." + aliasSub);
                this.registerSubCommands(parentLabel + "." + aliasSub, subcommand.getSubcommands());
            }
        }
    }

    /**
     * Enregistre un convertisseur d'argument.
     * @param typeClass La classe du type d'argument.
     * @param type Le type de l'argument.
     * @param converter Le convertisseur d'argument.
     */
    public <T> void registerConverter(Class<T> typeClass, String type, ArgumentConverter<T> converter) {
        this.typeConverters.put(type, new AbstractMap.SimpleEntry<>(typeClass, converter));
    }

    /**
     * Enregistre une commande personnalisée.
     * @param command La commande à enregistrer.
     * @param label le nom de la commande à enregistrer.
     */
    private void registerCommand(Command command, String label) throws TemplateArgumentNotExistException {
        try {
            plugin.getLogger().info("Register command " + label);
            ArrayList<Argument> args = command.getArgs();
            ArrayList<Argument> optArgs = command.getOptinalArgs();

            if(!this.checkTypeForArgs(args) || !this.checkTypeForArgs(optArgs)) {
                throw new TemplateArgumentNotExistException();
            }
            commands.put(label.toLowerCase(), command);
            String cmdLabel = label.split("\\.")[0].toLowerCase();
            if (commandMap.getCommand(cmdLabel) == null) {
                PluginCommand cmd = pluginConstructor.newInstance(cmdLabel, command.getPlugin());

                cmd.setExecutor(this);
                cmd.setTabCompleter(this);

                if(!commandMap.register(cmdLabel, cmd)) {
                    plugin.getLogger().severe("Unable to add the command " + cmdLabel);
                    return;
                }
            }
            if (!command.getDescription().equalsIgnoreCase("") && cmdLabel.equals(label)) {
                Objects.requireNonNull(commandMap.getCommand(cmdLabel)).setDescription(command.getDescription());
            }
            if (!command.getUsage().equalsIgnoreCase("") && cmdLabel.equals(label)) {
                Objects.requireNonNull(commandMap.getCommand(cmdLabel)).setUsage(command.getUsage());
            }

            String[] labelParts = label.split("\\.");
            int labelSize = labelParts.length;
            StringBuilder currentLabel = new StringBuilder();
            for (int i = 0; i < labelParts.length - 1; i++) {
                if (i > 0) {
                    currentLabel.append(".");
                }
                currentLabel.append(labelParts[i]);

                String completionPart = labelParts[i + 1];

                this.registerCompletion(currentLabel.toString(), i + 1, () -> Lists.newArrayList(completionPart));
            }

            for (int i = 0; i < args.size(); i++) {
                Argument arg = args.get(i);
                String[] parts = arg.arg().split(":");
                String type = parts[1].trim();
                ArgumentConverter<?> converter = this.typeConverters.get(type).getValue();
                if (arg.completion() != null) {
                    this.registerCompletion(label,labelSize + i, arg::completion);
                } else if (converter instanceof TabConverter tabConverter) {
                    this.registerCompletion(label,labelSize + i, tabConverter);
                } else {
                    this.registerCompletion(label, labelSize + i, ArrayList::new);
                }
            }
            for (int i = 0; i < optArgs.size(); i++) {
                Argument arg = optArgs.get(i);
                String[] parts = arg.arg().split(":");
                String type = parts[1].trim();
                ArgumentConverter<?> converter = this.typeConverters.get(type).getValue();
                if (arg.completion() != null) {
                    this.registerCompletion(label,labelSize + args.size() + i, arg::completion);
                } else if (converter instanceof TabConverter tabConverter) {
                    this.registerCompletion(label,labelSize + args.size() + i, tabConverter);
                } else {
                    this.registerCompletion(label, labelSize + args.size() + i, ArrayList::new);
                }
            }

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Enregistre un compléteur de commande.
     * @param label L'étiquette de la commande.
     * @param commandSize La taille de la commande.
     * @param converter Le convertisseur de complétion.
     */
    private void registerCompletion(String label, int commandSize, TabConverter converter) {
        Map<Integer, TabConverter> mapInner = this.completers.getOrDefault(label, new HashMap<>());
        TabConverter newConverter;
        TabConverter converterInner = mapInner.getOrDefault(commandSize, null);
        if(converterInner != null) {
            newConverter = () -> {
                List<String> completions = new ArrayList<>(converterInner.onCompletion());
                completions.addAll(converter.onCompletion());
                return completions;
            };
        } else {
            newConverter = converter;
        }
        mapInner.put(commandSize, newConverter);
        this.completers.put(label, mapInner);
    }

    /**
     * Vérifie si les types d'arguments sont corrects.
     * @param args La liste des arguments à vérifier.
     * @return true si les types sont corrects, sinon false.
     * @throws TemplateArgumentNotExistException Si le modèle d'argument n'existe pas.
     */
    private boolean checkTypeForArgs(ArrayList<Argument> args) throws TemplateArgumentNotExistException {
        for(String arg: args.stream().map(Argument::arg).toList()) {
            String[] parts = arg.split(":");

            if (parts.length != 2) {
                throw new TemplateArgumentNotExistException();
            }
            String type = parts[1].trim();
            if(!this.isGoodType(type)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parse les arguments fournis.
     * @param command La commande à laquelle les arguments sont associés.
     * @param args Les arguments à parser.
     * @return Les arguments parsés.
     * @throws TemplateArgumentNotExistException Si le modèle d'argument n'existe pas.
     * @throws ArgumentIncorrectException Si un argument est incorrect.
     */
    private Arguments parse(Command command, String[] args) throws TemplateArgumentNotExistException, ArgumentIncorrectException {
        Arguments arguments = new Arguments();
        ArrayList<Argument> templates = command.getArgs();
        for (int i = 0; i < templates.size(); i++) {
            String input = args[i];
            if (applyParsing(args, arguments, templates, i, input)) break;
        }

        ArrayList<Argument> optArgs = command.getOptinalArgs();
        if (optArgs.isEmpty()) {
            return arguments;
        }

        for (int i = 0; i < optArgs.size(); i++) {
            if (args.length > templates.size() + i) {
                String input = args[templates.size() + i];
                if (applyParsing(args, arguments, optArgs, i, input)) break;
            }
        }

        return arguments;
    }

    /**
     * Applique le parsing sur un argument spécifique.
     * @param args Les arguments fournis.
     * @param arguments Les arguments parsés.
     * @param templates Les modèles d'arguments.
     * @param i L'index de l'argument à parser.
     * @param input La valeur de l'argument.
     * @return true si le parsing a été appliqué avec succès, sinon false.
     * @throws TemplateArgumentNotExistException Si le modèle d'argument n'existe pas.
     * @throws ArgumentIncorrectException Si un argument est incorrect.
     */
    private boolean applyParsing(String[] args, Arguments arguments, ArrayList<Argument> templates, int i, String input) throws TemplateArgumentNotExistException, ArgumentIncorrectException {
        String template = templates.get(i).arg();
        String[] parts = template.split(":");

        if (parts.length != 2) {
            throw new TemplateArgumentNotExistException();
        }

        String key = parts[0].trim();
        String type = parts[1].trim();

        if (type.equals("infinite")) {
            StringBuilder builder = new StringBuilder();
            for (int ii = i; ii < args.length; ii++) {
                builder.append(args[ii]);
                if (ii < args.length - 1) {
                    builder.append(" ");
                }
            }
            arguments.add(key, String.class, builder.toString());
            return true;
        }

        if (typeConverters.containsKey(type)) {
            Map.Entry<Class<?>, ArgumentConverter<?>> converterWithType = typeConverters.get(type);
            Class<?> typeClass = converterWithType.getKey();
            ArgumentConverter<?> converter = converterWithType.getValue();
            Object obj = converter.apply(input);
            if (obj == null) {
                throw new ArgumentIncorrectException(input);
            }
            arguments.add(key, typeClass, obj);
        }
        return false;
    }

    /**
     * Vérifie si un type est correct.
     * @param type Le type à vérifier.
     * @return true si le type est correct, sinon false.
     */
    private boolean isGoodType(String type) {
        return this.typeConverters.containsKey(type);
    }

    /**
     * Exécute une commande lorsqu'elle est invoquée.
     *
     * @param sender Le commandant qui a invoqué la commande.
     * @param command L'objet de la commande invoquée.
     * @param label L'étiquette de la commande invoquée.
     * @param args Les arguments fournis avec la commande.
     * @return true si la commande a été exécutée avec succès, sinon false.
     */
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!plugin.isEnabled()) {
            return false;
        }

        if (!command.testPermission(sender)) {
            return true;
        }

        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());
            for (int x = 0; x < i; x++) {
                buffer.append(".").append(args[x].toLowerCase());
            }
            String cmdLabel = buffer.toString();
            if (commands.containsKey(cmdLabel)) {
                Command commandFramework = commands.get(cmdLabel);
                if (!command.getPermission().isEmpty() && !sender.hasPermission(command.getPermission())) {
                    sender.sendMessage("§cTu n'as pas la permission de faire cette commande.");
                    return true;
                }
                if (commandFramework.inGameOnly() && !(sender instanceof Player)) {
                    sender.sendMessage("§cCette commande n'est disponible qu'en jeu.");
                    return true;
                }
                int subCommand = cmdLabel.split("\\.").length - 1;
                String[] modArgs = new String[args.length - subCommand];
                if (args.length - subCommand >= 0)
                    System.arraycopy(args, subCommand, modArgs, 0, args.length - subCommand);

                if (modArgs.length < commandFramework.getArgs().size()) {
                    String usage = command.getUsage();
                    if (usage.isEmpty()) {
                        usage = "La commande n'a pas le bon nombre d'arguments.";
                    }
                    sender.sendMessage("§c" +usage);
                    return true;
                }

                if (!commandFramework.isInfiniteArgs() && (modArgs.length > commandFramework.getArgs().size() + commandFramework.getOptinalArgs().size())) {
                    String usage = command.getUsage();
                    if (usage.isEmpty()) {
                        usage = "La commande n'a pas le bon nombre d'arguments.";
                    }
                    sender.sendMessage("§c" + usage);
                    return true;
                }

                Arguments arguments;
                try {
                    arguments = this.parse(commandFramework, modArgs);
                } catch (TemplateArgumentNotExistException e) {
                    throw new RuntimeException(e);
                } catch (ArgumentIncorrectException e) {
                    sender.sendMessage("§cL'argument "+ "§a" + e.getInput() + "§c n'est pas reconnu dans l'usage de la commande.");
                    return true;
                }

                commandFramework.execute(sender, arguments);
                return true;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String label, String[] args) {
        String arg = args[args.length-1];
        for (int i = args.length; i >= 0; i--) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(label.toLowerCase());
            for (int x = 0; x < i; x++) {
                buffer.append(".").append(args[x].toLowerCase());
            }
            String cmdLabel = buffer.toString();
            if (this.completers.containsKey(cmdLabel)) {
                Map<Integer, TabConverter> map = this.completers.get(cmdLabel);
                if(map.containsKey(args.length)) {
                    TabConverter converter = map.get(args.length);
                    List<String> completer = converter.onCompletion().stream().filter(str -> str.toLowerCase().startsWith(arg.toLowerCase()) || str.equalsIgnoreCase(arg)).toList();
                    return completer.stream().filter(str -> {
                        String cmdLabelInner = cmdLabel + "." + str.toLowerCase();
                        if(this.commands.containsKey(cmdLabelInner)) {
                            Command endiaCommand = this.commands.get(cmdLabelInner);
                            return endiaCommand.getPermission().isEmpty() || commandSender.hasPermission(endiaCommand.getPermission());
                        }
                        return true;
                    }).collect(Collectors.toList());
                }
            }
        }

        return List.of();
    }

    /**
     * Récupère les commandes enregistrées.
     * @return Les commandes enregistrées.
     */
    public Map<String, Command> getCommands() {
        return commands;
    }
}
