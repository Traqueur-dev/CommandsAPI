package fr.traqueur.commands.annotations;

import fr.traqueur.commands.api.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class AnnotationsProvider<T extends JavaPlugin> {

    private final T plugin;
    private final CommandManager<T> manager;

    public AnnotationsProvider(T plugin, CommandManager<T> manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void addCommand(Object command) {
        Map<String,AnnotedCommand<T>> rootCommands = new HashMap<>();
        Stream.of(command.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(Command.class))
                .forEach(method -> {
                    if (method.getParameterCount() == 0 || !method.getParameterTypes()[0].equals(org.bukkit.command.CommandSender.class)) {
                        this.plugin.getLogger().warning("Command method " + method.getName() + " must have CommandSender as first parameter.");
                        return;
                    }
                    Command cmd = method.getAnnotation(Command.class);
                    AnnotedCommand<T> annotatedCommand = new AnnotedCommand<>(this.plugin, command, cmd, method);
                    rootCommands.put(cmd.name(), annotatedCommand);
                });

        Stream.of(command.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(SubCommand.class))
                .forEach(method -> {
                    if (method.getParameterCount() == 0 || !method.getParameterTypes()[0].equals(org.bukkit.command.CommandSender.class)) {
                        this.plugin.getLogger().warning("Command method " + method.getName() + " must have CommandSender as first parameter.");
                        return;
                    }
                    SubCommand subCmd = method.getAnnotation(SubCommand.class);
                    Command cmd = this.createCommand(subCmd);
                    AnnotedCommand<T> annotatedCommand = new AnnotedCommand<>(this.plugin, command, cmd, method);
                    if (subCmd.parent().isEmpty()) {
                        // If no parent is specified, register as a root command
                        rootCommands.put(subCmd.name(), annotatedCommand);
                    } else {
                        // Otherwise, find the parent command and add this as a sub-command
                        AnnotedCommand<T> parentCommand = rootCommands.get(subCmd.parent());
                        if (parentCommand != null) {
                            parentCommand.addSubCommand(annotatedCommand);
                        } else {
                            String[] parts = subCmd.parent().split("\\.");
                            boolean found = false;
                            for (AnnotedCommand<T> root : rootCommands.values()) {
                                if(!root.getName().equalsIgnoreCase(parts[0]) &&
                                        root.getAliases().stream().noneMatch(alias -> alias.equalsIgnoreCase(parts[0]))) {
                                    continue;
                                }

                                if(parts.length > 1) {
                                    fr.traqueur.commands.api.Command<T> foundCommand = findCommandByParts(root, parts, 1);
                                    if (foundCommand != null) {
                                        foundCommand.addSubCommand(annotatedCommand);
                                        found = true;
                                        break;
                                    }
                                } else {
                                    found = true;
                                    root.addSubCommand(annotatedCommand);
                                    break;
                                }
                            }
                            if(!found) {
                                this.plugin.getLogger().warning("No parent command found for " + subCmd.name() + ". It will not be registered.");
                            }
                        }
                    }
                });

        for (AnnotedCommand<T> rootCommand : rootCommands.values()) {
            this.manager.registerCommand(rootCommand);
        }

    }

    private fr.traqueur.commands.api.Command<T> findCommandByParts(fr.traqueur.commands.api.Command<T> current, String[] parts, int index) {
        if (current == null || index >= parts.length) {
            return current;
        }

        String currentPart = parts[index];

        for (fr.traqueur.commands.api.Command<T> sub : current.getSubcommands()) {
            if (sub.getName().equalsIgnoreCase(currentPart) ||
                    sub.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(currentPart))) {
                return findCommandByParts(sub, parts, index + 1);
            }
        }

        return null; // pas trouvé
    }

    private Command createCommand(SubCommand subCmd) {
        return new Command() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Command.class;
            }

            @Override
            public String name() {
                return subCmd.name();
            }

            @Override
            public String description() {
                return subCmd.description();
            }

            @Override
            public String permission() {
                return subCmd.permission();
            }

            @Override
            public String usage() {
                return subCmd.usage();
            }

            @Override
            public String[] aliases() {
                return subCmd.aliases();
            }
        };
    }

}
