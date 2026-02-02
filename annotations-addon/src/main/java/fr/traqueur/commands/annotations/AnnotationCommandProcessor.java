package fr.traqueur.commands.annotations;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandBuilder;
import fr.traqueur.commands.api.resolver.SenderResolver;
import fr.traqueur.commands.api.utils.Patterns;

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

    public List<Command<T, S>> register(Object... handlers) {
        List<Command<T, S>> allCommands = new ArrayList<>();
        for (Object handler : handlers) {
            allCommands.addAll(processHandler(handler));
        }
        return allCommands;
    }

    private List<Command<T, S>> processHandler(Object handler) {
        Class<?> clazz = handler.getClass();
        validateCommandContainer(clazz);

        collectTabCompleters(handler, clazz);

        List<CommandMethodInfo> commandMethods = collectCommandMethods(handler, clazz);
        Set<String> allPaths = extractAllPaths(commandMethods);

        Map<String, Command<T, S>> builtCommands = buildAllCommands(commandMethods, allPaths);
        Set<String> rootCommands = organizeHierarchy(commandMethods, allPaths, builtCommands);

        return registerRootCommands(rootCommands, builtCommands);
    }

    private void validateCommandContainer(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(CommandContainer.class)) {
            throw new IllegalArgumentException(
                    "Class must be annotated with @CommandContainer: " + clazz.getName()
            );
        }
    }

    private void collectTabCompleters(Object handler, Class<?> clazz) {
        tabCompleters.clear();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(TabComplete.class)) {
                processTabCompleter(handler, method);
            }
        }
    }

    private List<CommandMethodInfo> collectCommandMethods(Object handler, Class<?> clazz) {
        List<CommandMethodInfo> commandMethods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(fr.traqueur.commands.annotations.Command.class)) {
                fr.traqueur.commands.annotations.Command annotation =
                        method.getAnnotation(fr.traqueur.commands.annotations.Command.class);
                commandMethods.add(new CommandMethodInfo(handler, method, annotation.name()));
            }
        }
        commandMethods.sort(Comparator.comparingInt(info -> Patterns.DOT.split(info.name).length));
        return commandMethods;
    }

    private Set<String> extractAllPaths(List<CommandMethodInfo> commandMethods) {
        Set<String> allPaths = new HashSet<>();
        for (CommandMethodInfo info : commandMethods) {
            allPaths.add(info.name);
        }
        return allPaths;
    }

    private Map<String, Command<T, S>> buildAllCommands(List<CommandMethodInfo> commandMethods, Set<String> allPaths) {
        Map<String, Command<T, S>> builtCommands = new LinkedHashMap<>();
        for (CommandMethodInfo info : commandMethods) {
            String parentPath = getParentPath(info.name);
            boolean hasParentInBatch = parentPath != null && allPaths.contains(parentPath);
            Command<T, S> command = buildCommand(info.handler, info.method, info.name, hasParentInBatch);
            builtCommands.put(info.name, command);
        }
        return builtCommands;
    }

    private Set<String> organizeHierarchy(List<CommandMethodInfo> commandMethods, Set<String> allPaths,
                                          Map<String, Command<T, S>> builtCommands) {
        Set<String> rootCommands = new LinkedHashSet<>();
        for (CommandMethodInfo info : commandMethods) {
            String parentPath = getParentPath(info.name);
            if (parentPath != null && allPaths.contains(parentPath)) {
                builtCommands.get(parentPath).addSubCommand(builtCommands.get(info.name));
            } else {
                rootCommands.add(info.name);
            }
        }
        return rootCommands;
    }

    private List<Command<T, S>> registerRootCommands(Set<String> rootCommands, Map<String, Command<T, S>> builtCommands) {
        List<Command<T, S>> registeredCommands = new ArrayList<>();
        for (String rootPath : rootCommands) {
            Command<T, S> command = builtCommands.get(rootPath);
            manager.registerCommand(command);
            registeredCommands.add(command);
        }
        return registeredCommands;
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

        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];

            if (i == 0 && isSenderParameter(param)) {
                continue;
            }

            registerArgument(builder, param, commandPath);
        }
    }

    private boolean isSenderParameter(Parameter param) {
        Class<?> paramType = param.getType();
        Class<?> senderType = (paramType == Optional.class) ? extractOptionalType(param) : paramType;
        return senderResolver.canResolve(senderType);
    }

    private void registerArgument(CommandBuilder<T, S> builder, Parameter param, String commandPath) {
        String argName = getArgumentName(param);
        Class<?> argType = resolveArgumentType(param);
        boolean isOptional = param.getType() == Optional.class;
        TabCompleter<S> completer = getTabCompleter(commandPath, argName);

        if (isOptional) {
            builder.optionalArg(argName, argType, completer);
        } else {
            builder.arg(argName, argType, completer);
        }
    }

    private String getArgumentName(Parameter param) {
        Arg argAnnotation = param.getAnnotation(Arg.class);
        return (argAnnotation != null) ? argAnnotation.value() : param.getName();
    }

    private Class<?> resolveArgumentType(Parameter param) {
        if (param.isAnnotationPresent(Infinite.class)) {
            return fr.traqueur.commands.api.arguments.Infinite.class;
        }
        Class<?> paramType = param.getType();
        return (paramType == Optional.class) ? extractOptionalType(param) : paramType;
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
                Object result = invokeTabCompleter(tcMethod, sender, args);
                return (List<String>) result;
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to invoke tab completer for command '" + commandPath +
                        "', argument '" + argName + "', method '" + tcMethod.method.getName() + "'", e);
            }
        };
    }

    private Object invokeTabCompleter(TabCompleterMethod tcMethod, S sender, List<String> args) throws Exception {
        Parameter[] params = tcMethod.method.getParameters();

        if (params.length == 0) {
            return tcMethod.method.invoke(tcMethod.handler);
        }

        Object resolvedSender = senderResolver.resolve(sender, params[0].getType());
        if (params.length == 1) {
            return tcMethod.method.invoke(tcMethod.handler, resolvedSender);
        }

        String current = !args.isEmpty() ? args.getLast() : "";
        return tcMethod.method.invoke(tcMethod.handler, resolvedSender, current);
    }

    private void invokeMethod(Object handler, Method method, S sender, Arguments args) {
        try {
            Parameter[] params = method.getParameters();
            Object[] invokeArgs = buildInvokeArgs(params, sender, args);
            method.invoke(handler, invokeArgs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke command method: " + method.getName(), e);
        }
    }

    private Object[] buildInvokeArgs(Parameter[] params, S sender, Arguments args) {
        Object[] invokeArgs = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];

            if (i == 0 && isSenderParameter(param)) {
                invokeArgs[i] = resolveSender(param, sender);
            } else {
                invokeArgs[i] = resolveArgument(param, args);
            }
        }
        return invokeArgs;
    }

    private Object resolveSender(Parameter param, S sender) {
        Class<?> paramType = param.getType();
        boolean isOptional = paramType == Optional.class;
        Class<?> senderType = isOptional ? extractOptionalType(param) : paramType;
        Object resolved = senderResolver.resolve(sender, senderType);
        return isOptional ? Optional.ofNullable(resolved) : resolved;
    }

    private Object resolveArgument(Parameter param, Arguments args) {
        String argName = getArgumentName(param);
        boolean isOptional = param.getType() == Optional.class;
        return isOptional ? args.getOptional(argName) : args.get(argName);
    }

    private record CommandMethodInfo(Object handler, Method method, String name) {}
    private record TabCompleterMethod(Object handler, Method method) {}
}