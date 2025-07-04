package fr.traqueur.commands.api.updater;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * This class is used to check if the plugin is up to date
 */
public class Updater {

    /**
     * Private constructor to prevent instantiation
     */
    private Updater() {}

    /**
     * Check if the plugin is up to date and log a warning if it's not
     */
    public static void checkUpdates() {
        if(!Updater.isUpToDate()) {
            Logger.getLogger("CommandsAPI").warning("The framework is not up to date, the latest version is " + Updater.fetchLatestVersion());
        }
    }

    /**
     * Get the version of the plugin
     * @return The version of the plugin
     */
    public static String getVersion() {
        Properties prop = new Properties();
        try {
            prop.load(Updater.class.getClassLoader().getResourceAsStream("commands.properties"));
            return prop.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if the plugin is up to date
     * @return True if the plugin is up to date, false otherwise
     */
    public static boolean isUpToDate() {
        try {
            String latestVersion = fetchLatestVersion();
            return getVersion().equals(latestVersion);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the latest version of the plugin
     * @return The latest version of the plugin
     */
    public static String fetchLatestVersion() {
        try {
            URL url = URI.create("https://api.github.com/repos/Traqueur-dev/CommandsAPI/releases/latest").toURL();
            String responseString = getString(url);
            int tagNameIndex = responseString.indexOf("\"tag_name\"");
            int start = responseString.indexOf('\"', tagNameIndex + 10) + 1;
            int end = responseString.indexOf('\"', start);
            return responseString.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the latest version of the plugin
     * @return The latest version of the plugin
     */
    private static String getString(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
        } finally {
            connection.disconnect();
        }

        return response.toString();
    }
}