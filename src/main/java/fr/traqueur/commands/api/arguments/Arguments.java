package fr.traqueur.commands.api.arguments;

import fr.traqueur.commands.api.exceptions.ArgumentNotExistException;
import fr.traqueur.commands.api.exceptions.NoGoodTypeArgumentException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cette classe représente un ensemble d'arguments avec leurs clés et valeurs associées.
 */
public class Arguments {
    private final HashMap<ArgumentKey<?>, Object> arguments;

    /**
     * Constructeur pour créer une nouvelle instance de Arguments.
     */
    public Arguments() {
        this.arguments = new HashMap<>();
    }

    /**
     * Obtient la valeur de l'argument spécifié.
     * @param argument Le nom de l'argument.
     * @param <T> Le type de la valeur de l'argument.
     * @return La valeur de l'argument spécifié.
     */
    public <T> T get(String argument) {
        try {
            Optional<T> value = this.getOptional(argument);
            if (value.isEmpty()) {
                throw new ArgumentNotExistException();
            }
            return value.get();
        } catch (ArgumentNotExistException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtient la valeur de l'argument spécifié sous forme d'Optional.
     * @param argument Le nom de l'argument.
     * @param <T> Le type de la valeur de l'argument.
     * @return La valeur de l'argument spécifié sous forme d'Optional.
     */
    public <T> Optional<T> getOptional(String argument) {
        try {
            for (Map.Entry<ArgumentKey<?>, Object> entry : arguments.entrySet()) {
                ArgumentKey<?> argumentKey = entry.getKey();
                String key = argumentKey.getKey();
                if (!argument.equals(key)) {
                    continue;
                }
                Class<?> type = argumentKey.getType();
                Object value = entry.getValue();

                if (!type.isInstance(value)) {
                    throw new NoGoodTypeArgumentException();
                }

                return Optional.ofNullable((T) value);
            }
            return Optional.empty();
        } catch (NoGoodTypeArgumentException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Ajoute un nouvel argument avec sa clé et sa valeur associée.
     * @param key La clé de l'argument.
     * @param type Le type de l'argument.
     * @param object La valeur de l'argument.
     */
    public void add(String key, Class<?> type, Object object) {
        ArgumentKey<?> argumentKey = ArgumentKey.of(key, type);
        this.arguments.put(argumentKey, object);
    }
}
