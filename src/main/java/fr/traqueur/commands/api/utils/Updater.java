package fr.traqueur.commands.api.utils;

import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This class is used to check for updates on GitHub
 */
public class Updater {

    /**
     * Get the version of the plugin
     * @return The version of the plugin
     */
    public static String getVersion() {
        Properties prop = new Properties();
        try {
            prop.load(Updater.class.getClassLoader().getResourceAsStream("version.properties"));
            return prop.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check for updates
     * @param plugin The plugin to check for updates
     */
    public static void checkForUpdates(Plugin plugin) {
        Logger logger = plugin.getLogger();
        GitHubReleaseAPI api;
        try {
            api = new GitHubReleaseAPI("TabCompleter", "hyperdefined");
        } catch (IOException e) {
            logger.warning("Unable to check updates!");
            logger.warning(e.getMessage());
            return;
        }
        GitHubRelease current = api.getReleaseByTag(getVersion());
        GitHubRelease latest = api.getLatestVersion();
        if (current == null) {
            logger.warning("You are running a version that does not exist on GitHub. If you are in a dev environment, you can ignore this. Otherwise, this is a bug!");
            return;
        }
        int buildsBehind = api.getBuildsBehind(current);
        if (buildsBehind == 0) {
            logger.info("You are running the latest version.");
        } else {
            logger.warning("A new version is available (" + latest.getTagVersion() + ")! You are running version " + current.getTagVersion() + ". You are " + buildsBehind + " version(s) behind.");
        }
    }
}
