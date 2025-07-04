package fr.traqueur.commands.annotations;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import fr.traqueur.commands.api.CommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class AnnotedCommand<T extends JavaPlugin> extends Command<T> {

    private final Object instance;
    private final Method method;

    private final Map<String, Class<?>> arguments;
    private final Map<String, Class<?>> optionalArguments;

    public AnnotedCommand(T plugin, Object instance, fr.traqueur.commands.annotations.Command command, Method method) {
        super(plugin, command.name());
        this.method = method;
        this.instance = instance;
        this.arguments = new HashMap<>();
        this.optionalArguments = new HashMap<>();

        this.setPermission(command.permission());
        this.setDescription(command.description());
        this.setUsage(command.usage());
        for (String alias : command.aliases()) {
            this.addAlias(alias);
        }


        int size = method.getParameterCount() - 1;
        for (int i = 1; i <= size; i++) {
            Parameter parameter = method.getParameters()[i];
            boolean optional = parameter.getType().equals(Optional.class);
            Class<?> type = parameter.getType();
            if (optional) {
                type = (Class<?>) ((java.lang.reflect.ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
            }
            String parameterName = parameter.getName();
            boolean infinite = false;
            if(parameter.isAnnotationPresent(Arg.class)) {
                Arg argAnnotation = parameter.getAnnotation(Arg.class);
                parameterName = argAnnotation.name();
                infinite = argAnnotation.infinite();
            }
            if(optional && infinite) {
                throw new IllegalArgumentException("Optional arguments cannot be infinite: " + parameterName);
            }

            if(type != String.class && infinite) {
                throw new IllegalArgumentException("Infinite arguments must be of type String: " + parameterName);
            }

            if (optional) {
                this.optionalArguments.put(parameterName, type);
                this.addOptionalArgs(parameterName, type);
            } else {
                if(infinite) {
                    this.addArgs(parameterName + CommandManager.TYPE_PARSER + "infinite");
                }

                this.arguments.put(parameterName, type);
                this.addArgs(parameterName, type);
            }
        }
    }

    @Override
    public void execute(CommandSender sender, Arguments args) {
        method.setAccessible(true);
        List<Object> parameters = new ArrayList<>();
        parameters.add(sender);
        for (Map.Entry<String, Class<?>> stringClassEntry : this.arguments.entrySet()) {
            String argumentName = stringClassEntry.getKey();
            Class<?> argumentType = stringClassEntry.getValue();
            parameters.add(args.getAs(argumentName, argumentType, null));
        }
        for (Map.Entry<String, Class<?>> stringClassEntry : this.optionalArguments.entrySet()) {
            String argumentName = stringClassEntry.getKey();
            Class<?> argumentType = stringClassEntry.getValue();
            parameters.add(args.getAs(argumentName, argumentType));
        }
        try {
            method.invoke(instance, parameters.toArray());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }
}
