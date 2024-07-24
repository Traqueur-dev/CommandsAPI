package fr.traqueur.commands.updater;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

public class Updater {

    private static String getVersion() {
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
            return latestVersion.equals(getVersion());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String fetchLatestVersion() throws IOException {
        URL url = new URL("https://api.github.com/repos/Traqueur-dev/CommandsAPI/releases/latest");
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

        String responseString = response.toString();
        int tagNameIndex = responseString.indexOf("\"tag_name\"");
        int start = responseString.indexOf('\"', tagNameIndex + 10) + 1;
        int end = responseString.indexOf('\"', start);
        return responseString.substring(start, end);
    }
}