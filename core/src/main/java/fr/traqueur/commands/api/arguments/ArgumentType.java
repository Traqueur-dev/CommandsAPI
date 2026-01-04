package fr.traqueur.commands.api.arguments;

public sealed interface ArgumentType permits ArgumentType.Simple, ArgumentType.Infinite {

    static ArgumentType of(Class<?> clazz) {
        if (clazz.isAssignableFrom(fr.traqueur.commands.api.arguments.Infinite.class)) {
            return Infinite.INSTANCE;
        }
        return new Simple(clazz);
    }

    String key();

    /**
     * Check if this is the infinite type.
     *
     * @return true if infinite
     */
    default boolean isInfinite() {
        return this instanceof Infinite;
    }

    record Simple(Class<?> clazz) implements ArgumentType {

        @Override
        public String key() {
            return clazz.getName().toLowerCase();
        }

    }

    record Infinite() implements ArgumentType {
        public static final Infinite INSTANCE = new Infinite();

        @Override
        public String key() {
            return "infinite";
        }
    }

}
