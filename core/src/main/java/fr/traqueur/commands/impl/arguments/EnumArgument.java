package fr.traqueur.commands.impl.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnumArgument<T extends Enum<T>, S> implements ArgumentConverter<Enum<T>>, TabCompleter<S> {

    public static <E extends Enum<E>, S> EnumArgument<E, S> of(Class<E> enumClass) {
        return new EnumArgument<>(enumClass);
    }

    private final Class<T> clazz;

    public EnumArgument(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Enum<T> apply(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(clazz, s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public List<String> onCompletion(S sender, List<String> args) {
        return Arrays.stream(clazz.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
