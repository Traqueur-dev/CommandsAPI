package fr.traqueur.commands.impl.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An argument converter for enum types, allowing conversion from string to enum and providing tab completion.
 *
 * @param <T> The type of the enum.
 * @param <S> The type of the sender (e.g., player, console).
 */
public class EnumArgument<T extends Enum<T>, S> implements ArgumentConverter<T>, TabCompleter<S> {

    /**
     * The class of the enum type this argument converter handles.
     */
    private final Class<T> clazz;

    /**
     * Constructs a new EnumArgument for the specified enum class.
     *
     * @param clazz The class of the enum type
     */
    public EnumArgument(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Creates a new EnumArgument instance for the specified enum class.
     *
     * @param enumClass The class of the enum
     * @param <E>       The type of the enum
     * @param <S>       The type of the sender (e.g., player, console)
     * @return A new instance of EnumArgument
     */
    public static <E extends Enum<E>, S> EnumArgument<E, S> of(Class<E> enumClass) {
        return new EnumArgument<>(enumClass);
    }

    /**
     * Gets the class of the enum type this argument converter handles.
     *
     * @return The enum class
     */
    @Override
    public T apply(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(clazz, s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Gets the class of the enum type this argument converter handles.
     *
     * @return The enum class
     */
    @Override
    public List<String> onCompletion(S sender, List<String> args) {
        return Arrays.stream(clazz.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
