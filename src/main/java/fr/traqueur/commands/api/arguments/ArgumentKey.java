package fr.traqueur.commands.api.arguments;

/**
 * Cette classe représente une clé d'argument qui associe un nom et un type.
 * @param <T> Le type de la valeur associée à la clé.
 */
public class ArgumentKey<T> {

    private final String name;
    private final Class<T> type;

    /**
     * Constructeur privé pour créer une nouvelle instance de ArgumentKey.
     * @param name Le nom de la clé.
     * @param type Le type de la valeur associée à la clé.
     */
    private ArgumentKey(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Méthode statique pour créer une nouvelle instance de ArgumentKey.
     * @param name Le nom de la clé.
     * @param type Le type de la valeur associée à la clé.
     * @param <T> Le type de la valeur associée à la clé.
     * @return Une nouvelle instance de ArgumentKey avec le nom et le type spécifiés.
     */
    public static <T> ArgumentKey<T> of(String name, Class<T> type) {
        return new ArgumentKey<>(name, type);
    }

    /**
     * Obtient le nom de la clé.
     * @return Le nom de la clé.
     */
    public String getKey() {
        return name;
    }

    /**
     * Obtient le type de la valeur associée à la clé.
     * @return Le type de la valeur associée à la clé.
     */
    public Class<T> getType() {
        return type;
    }
}
