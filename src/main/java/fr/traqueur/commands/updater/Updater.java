package fr.traqueur.commands.updater;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

public class Updater {

    public static void checkUpdates() {
        if(!Updater.isUpToDate()) {
            Logger.getLogger("CommandsAPI").warning("The framework is not up to date, the latest version is " + Updater.fetchLatestVersion());
        }
    }

    public static String getVersion() {
        Properties prop = new Properties();
        try {
            prop.load(Updater.class.getClassLoader().getResourceAsStream("version.properties"));
            return prop.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isUpToDate() {
        try {
            String latestVersion = fetchLatestVersion();
            return getVersion().equals(latestVersion);
        } catch (Exception e) {
            return false;
        }
    }

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