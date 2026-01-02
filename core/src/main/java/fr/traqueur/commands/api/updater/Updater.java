package fr.traqueur.commands.api.updater;

import fr.traqueur.commands.api.exceptions.UpdaterInitializationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Non-blocking updater using Reposilite (releases -> snapshots fallback)
 */
public final class Updater {

    private static final String VERSION_PROPERTY_FILE = "commands.properties";

    private static final URL RELEASES_URL;
    private static final URL SNAPSHOTS_URL;

    private static volatile URL RESOLVED_METADATA_URL;

    private static Logger LOGGER = Logger.getLogger("CommandsAPI");

    static {
        try {
            RELEASES_URL = URI.create(
                    "https://repo.groupez.dev/releases/fr/traqueur/commands/core/maven-metadata.xml"
            ).toURL();

            SNAPSHOTS_URL = URI.create(
                    "https://repo.groupez.dev/snapshots/fr/traqueur/commands/core/maven-metadata.xml"
            ).toURL();
        } catch (MalformedURLException e) {
            throw new UpdaterInitializationException("Failed to initialize updater URLs", e);
        }
    }

    private Updater() {}

    public static void setLogger(Logger logger) {
        LOGGER = logger;
    }

    /* ------------------------------------------------------------ */
    /* Public API                                                   */
    /* ------------------------------------------------------------ */

    /**
     * Async update check (non-blocking)
     */
    public static void checkUpdates() {
        fetchLatestVersionAsync().thenAccept(latest -> {
            if (latest == null) {
                return;
            }

            String current = getVersion();

            if (!current.equals(latest)) {
                LOGGER.warning("⚠ CommandsAPI is not up to date!");
                LOGGER.warning("Current: " + current + " | Latest: " + latest);
            } else {
                LOGGER.info("✅ CommandsAPI is up to date (" + current + ")");
            }
        });
    }

    /**
     * Async latest version fetch
     */
    public static CompletableFuture<String> fetchLatestVersionAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL metadataUrl = resolveMetadataUrl();
                if (metadataUrl == null) {
                    return null;
                }

                HttpURLConnection connection =
                        (HttpURLConnection) metadataUrl.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                try (InputStream is = connection.getInputStream()) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(is);

                    // Priority: <release>
                    NodeList release = document.getElementsByTagName("release");
                    if (release.getLength() > 0) {
                        String value = release.item(0).getTextContent();
                        if (!value.isEmpty()) {
                            return value;
                        }
                    }

                    // Fallback: <latest>
                    NodeList latest = document.getElementsByTagName("latest");
                    if (latest.getLength() > 0) {
                        String value = latest.item(0).getTextContent();
                        if (!value.isEmpty()) {
                            return value;
                        }
                    }

                    // Fallback: SNAPSHOT
                    NodeList versions = document.getElementsByTagName("version");
                    String snapshot = null;

                    for (int i = 0; i < versions.getLength(); i++) {
                        String v = versions.item(i).getTextContent();
                        if (v.endsWith("-SNAPSHOT")) {
                            snapshot = v;
                        }
                    }

                    return snapshot;
                }
            } catch (Exception e) {
                LOGGER.warning("Failed to check updates: " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Current version from properties
     */
    public static String getVersion() {
        Properties prop = new Properties();
        try (InputStream is = Updater.class
                .getClassLoader()
                .getResourceAsStream(VERSION_PROPERTY_FILE)) {

            if (is == null) {
                throw new IllegalStateException("commands.properties not found");
            }

            prop.load(is);
            return prop.getProperty("version");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* ------------------------------------------------------------ */
    /* Internal helpers                                             */
    /* ------------------------------------------------------------ */

    /**
     * Resolve metadata URL once (releases -> snapshots)
     */
    private static URL resolveMetadataUrl() {
        if (RESOLVED_METADATA_URL != null) {
            return RESOLVED_METADATA_URL;
        }

        synchronized (Updater.class) {
            if (RESOLVED_METADATA_URL != null) {
                return RESOLVED_METADATA_URL;
            }

            if (isValidMetadata(RELEASES_URL)) {
                RESOLVED_METADATA_URL = RELEASES_URL;
                LOGGER.info("Update source: releases");
                return RESOLVED_METADATA_URL;
            }

            if (isValidMetadata(SNAPSHOTS_URL)) {
                RESOLVED_METADATA_URL = SNAPSHOTS_URL;
                LOGGER.info("Update source: snapshots");
                return RESOLVED_METADATA_URL;
            }

            LOGGER.warning("No valid update source found (releases/snapshots)");
            return null;
        }
    }

    /**
     * Lightweight HEAD check
     */
    private static boolean isValidMetadata(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int code = connection.getResponseCode();
            return code >= 200 && code < 300;
        } catch (Exception e) {
            return false;
        }
    }
}
