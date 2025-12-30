package fr.traqueur.commands.api.updater;

import fr.traqueur.commands.api.exceptions.UpdaterInitializationException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * This class is used to check if the plugin is up to date
 */
public class Updater {

    private static final String VERSION_PROPERTY_FILE = "commands.properties";
    private static final URL URL_LATEST_RELEASE;
    private static Logger LOGGER = Logger.getLogger("CommandsAPI");

    static {
        try {
            URL_LATEST_RELEASE = URI.create(
                    "https://repo.groupez.dev/releases/fr/traqueur/commands/core/maven-metadata.xml"
            ).toURL();
        } catch (MalformedURLException e) {
            throw new UpdaterInitializationException("Failed to initialize updater URL", e);
        }
    }

    private Updater() {}

    public static void setLogger(Logger logger) {
        Updater.LOGGER = logger;
    }

    public static void checkUpdates() {
        try {
            String latest = fetchLatestVersion();
            String current = getVersion();

            if (latest != null && !latest.equals(current)) {
                LOGGER.warning("⚠ CommandsAPI is not up to date!");
                LOGGER.warning("Current: " + current + " | Latest: " + latest);
            }
        } catch (Exception ignored) {
        }
    }

    public static String getVersion() {
        Properties prop = new Properties();
        try (InputStream is = Updater.class
                .getClassLoader()
                .getResourceAsStream(VERSION_PROPERTY_FILE)) {

            if (is == null) {
                throw new RuntimeException("commands.properties not found");
            }

            prop.load(is);
            return prop.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isUpToDate() {
        String latest = fetchLatestVersion();
        return latest != null && getVersion().equals(latest);
    }

    /**
     * Fetch latest version from Reposilite maven-metadata.xml
     */
    public static String fetchLatestVersion() {
        try {
            HttpURLConnection connection =
                    (HttpURLConnection) URL_LATEST_RELEASE.openConnection();

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");

            try (InputStream is = connection.getInputStream()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(is);

                // Priorité à <release>, fallback <latest>
                NodeList release = document.getElementsByTagName("release");
                if (release.getLength() > 0) {
                    return release.item(0).getTextContent();
                }

                NodeList latest = document.getElementsByTagName("latest");
                if (latest.getLength() > 0) {
                    return latest.item(0).getTextContent();
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to fetch latest version: " + e.getMessage());
        }

        return null;
    }
}
