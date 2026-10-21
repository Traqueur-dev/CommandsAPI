package fr.traqueur.commands.api.updater;

/**
 * Reads the server-wide switch of the updater.
 * <p>
 * Kept out of {@link Updater} so that the switch is honoured even when the updater's own
 * static methods are replaced, and so that it can be read without touching the network.
 * </p>
 *
 * @since 5.4.0
 */
final class UpdaterSettings {

    /**
     * The system property that turns the update check off when set to {@code false}.
     */
    static final String PROPERTY = "commands.updater";

    private UpdaterSettings() {
    }

    /**
     * Tells whether the update check is allowed to run.
     *
     * @return false when {@code -Dcommands.updater=false} is set, true otherwise.
     */
    static boolean isEnabled() {
        return !"false".equalsIgnoreCase(System.getProperty(PROPERTY, "true").trim());
    }
}
