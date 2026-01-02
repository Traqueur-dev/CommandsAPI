package fr.traqueur.commands.annotations;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandBuilder;
import fr.traqueur.commands.api.resolver.SenderResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Processes annotated command containers and registers them with the CommandManager.
 *
 * @param <T> plugin type
 * @param <S> sender type
 * @since 5.0.0
 */
public class AnnotationCommandProcessor<T, S> {

    private final CommandManager<T, S> manager;
    private final SenderResolver<S> senderResolver;
    private final Map<String, TabCompleterMethod> tabCompleters = new HashMap<>();

    public AnnotationCommandProcessor(CommandManager<T, S> manager) {
        this.manager = manager;
        this.senderResolver = manager.getPlatform().getSenderResolver();
    }

    public void register(Object... handlers) {
        for (Object handler : handlers) {
            processHandler(handler);
        }
    }

    private void processHandler(Object handler) {
        Class<?> clazz = handler.getClass();

        if (!clazz.isAnnotationPresent(CommandContainer.class)) {
            throw new IllegalArgumentException(
                    "Class must be annotated with @CommandContainer: " + clazz.getName()
            );
        }

        // First pass: collect all @TabComplete methods
        tabCompleters.clear();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(TabComplete.class)) {
                processTabCompleter(handler, method);
            }
        }

        // Second pass: collect all @Command methods and sort by depth
        List<CommandMethodInfo> commandMethods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(fr.traqueur.commands.annotations.Command.class)) {
                fr.traqueur.commands.annotations.Command annotation =
                        method.getAnnotation(fr.traqueur.commands.annotations.Command.class);
                commandMethods.add(new CommandMethodInfo(handler, method, annotation.name()));
            }
        }

        // Sort by depth (parents first)
        commandMethods.sort(Comparator.comparingInt(info -> info.name.split("\\.").length));

        // Collect all command paths to determine which have parents defined
        Set<String> allPaths = new HashSet<>();
        for (CommandMethodInfo info : commandMethods) {
            allPaths.add(info.name);
        }

        // Third pass: build ALL commands first
        Map<String, Command<T, S>> builtCommands = new LinkedHashMap<>();
        Set<String> rootCommands = new LinkedHashSet<>();

        for (CommandMethodInfo info : commandMethods) {
            String parentPath = getParentPath(info.name);
            boolean hasParentInBatch = parentPath != null && allPaths.contains(parentPath);

            Command<T, S> command = buildCommand(info.handler, info.method, info.name, hasParentInBatch);
            builtCommands.put(info.name, command);
        }

        // Fourth pass: organize hierarchy (add subcommands to parents)
        for (CommandMethodInfo info : commandMethods) {
            String parentPath = getParentPath(info.name);

            if (parentPath != null && allPaths.contains(parentPath)) {
                Command<T, S> parent = builtCommands.get(parentPath);
                Command<T, S> child = builtCommands.get(info.name);
                parent.addSubCommand(child);
            } else {
                rootCommands.add(info.name);
            }
        }

        // Fifth pass: register only root commands
        for (String rootPath : rootCommands) {
            Command<T, S> rootCommand = builtCommands.get(rootPath);
            manager.registerCommand(rootCommand);
        }
    }

    private String getParentPath(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot == -1) {
            return null;
        }
        return path.substring(0, lastDot);
    }

    private String getCommandName(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot == -1) {
            return path;
        }
        return path.substring(lastDot + 1);
    }

    private Command<T, S> buildCommand(Object handler, Method method, String fullPath, boolean hasParentInBatch) {
        fr.traqueur.commands.annotations.Command annotation =
                method.getAnnotation(fr.traqueur.commands.annotations.Command.class);

        String commandName = hasParentInBatch ? getCommandName(fullPath) : fullPath;

        CommandBuilder<T, S> builder = manager.command(commandName)
                .description(annotation.description())
                .permission(annotation.permission())
                .usage(annotation.usage());

        if (method.isAnnotationPresent(Alias.class)) {
            Alias aliasAnnotation = method.getAnnotation(Alias.class);
            builder.aliases(aliasAnnotation.value());
        }

        processParameters(builder, method, fullPath);

        Parameter[] params = method.getParameters();
        if (params.length > 0) {
            Class<?> senderType = params[0].getType();
            // Handle Optional<Player> as sender (extract inner type)
            if (senderType == Optional.class) {
                senderType = extractOptionalType(params[0]);
            }
            if (senderResolver.isGameOnly(senderType)) {
                builder.gameOnly();
            }
        }

        method.setAccessible(true);
        builder.executor((sender, args) -> invokeMethod(handler, method, sender, args));

        return builder.build();
    }

    private void processTabCompleter(Object handler, Method method) {
        TabComplete annotation = method.getAnnotation(TabComplete.class);
        String key = annotation.command() + ":" + annotation.arg();

        method.setAccessible(true);
        tabCompleters.put(key, new TabCompleterMethod(handler, method));
    }

    private void processParameters(CommandBuilder<T, S> builder, Method method, String commandPath) {
        Parameter[] params = method.getParameters();
        Type[] genericTypes = method.getGenericParameterTypes();

        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            Class<?> paramType = param.getType();

            // First parameter is sender (skip it for args)
            if (i == 0) {
                Class<?> senderType = paramType;
                if (paramType == Optional.class) {
                    senderType = extractOptionalType(param);
                }
                if (senderResolver.canResolve(senderType)) {
                    continue;
                }
            }

            // Must have @Arg annotation
            Arg argAnnotation = param.getAnnotation(Arg.class);
            if (argAnnotation == null) {
                throw new IllegalArgumentException(
                        "Parameter '" + param.getName() + "' in method '" + method.getName() +
                                "' must be annotated with @Arg or be the sender type"
                );
            }

            String argName = argAnnotation.value();
            boolean isOptional = paramType == Optional.class;
            boolean isInfinite = param.isAnnotationPresent(Infinite.class);

            // Determine the actual argument type
            Class<?> argType;
            if (isOptional) {
                argType = extractOptionalType(param);
            } else {
                argType = paramType;
            }

            // If @Infinite, use Infinite.class as the type
            if (isInfinite) {
                argType = fr.traqueur.commands.api.arguments.Infinite.class;
            }

            // Get tab completer if exists
            TabCompleter<S> completer = getTabCompleter(commandPath, argName);

            // Add argument to builder
            if (isOptional) {
                if (completer != null) {
                    builder.optionalArg(argName, argType, completer);
                } else {
                    builder.optionalArg(argName, argType);
                }
            } else {
                if (completer != null) {
                    builder.arg(argName, argType, completer);
                } else {
                    builder.arg(argName, argType);
                }
            }
        }
    }

    /**
     * Extract the inner type from Optional<T>.
     */
    private Class<?> extractOptionalType(Parameter param) {
        Type genericType = param.getParameterizedType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> innerClass) {
                return innerClass;
            }
        }
        throw new IllegalArgumentException(
                "Cannot extract type from Optional parameter: " + param.getName()
        );
    }

    @SuppressWarnings("unchecked")
    private TabCompleter<S> getTabCompleter(String commandPath, String argName) {
        String key = commandPath + ":" + argName;
        TabCompleterMethod tcMethod = tabCompleters.get(key);

        if (tcMethod == null) {
            return null;
        }

        return (sender, args) -> {
            try {
                Object result;
                Parameter[] params = tcMethod.method.getParameters();

                if (params.length == 0) {
                    result = tcMethod.method.invoke(tcMethod.handler);
                } else if (params.length == 1) {
                    Object resolvedSender = senderResolver.resolve(sender, params[0].getType());
                    result = tcMethod.method.invoke(tcMethod.handler, resolvedSender);
                } else {
                    Object resolvedSender = senderResolver.resolve(sender, params[0].getType());
                    String current = !args.isEmpty() ? args.getLast() : "";
                    result = tcMethod.method.invoke(tcMethod.handler, resolvedSender, current);
                }

                return (List<String>) result;
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke tab completer", e);
            }
        };
    }

    private void invokeMethod(Object handler, Method method, S sender, Arguments args) {
        try {
            Parameter[] params = method.getParameters();
            Object[] invokeArgs = new Object[params.length];

            for (int i = 0; i < params.length; i++) {
                Parameter param = params[i];
                Class<?> paramType = param.getType();
                boolean isOptional = paramType == Optional.class;

                // First param: sender
                if (i == 0) {
                    Class<?> senderType = isOptional ? extractOptionalType(param) : paramType;
                    if (senderResolver.canResolve(senderType)) {
                        Object resolved = senderResolver.resolve(sender, senderType);
                        invokeArgs[i] = isOptional ? Optional.ofNullable(resolved) : resolved;
                        continue;
                    }
                }

                // Other params: @Arg
                Arg argAnnotation = param.getAnnotation(Arg.class);
                if (argAnnotation != null) {
                    String argName = argAnnotation.value();

                    if (isOptional) {
                        invokeArgs[i] = args.getOptional(argName);
                    } else {
                        invokeArgs[i] = args.get(argName);
                    }
                }
            }

            method.invoke(handler, invokeArgs);

        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke command method: " + method.getName(), e);
        }
    }

    private record CommandMethodInfo(Object handler, Method method, String name) {}
    private record TabCompleterMethod(Object handler, Method method) {}
}