package fr.traqueur.commands.api.updater;

import fr.traqueur.commands.test.mocks.MockCommandManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class UpdaterOptInTest {

    private Logger logger;
    private final List<LogRecord> records = new ArrayList<>();
    private final Handler handler = new Handler() {
        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

    @BeforeEach
    void setUp() {
        logger = Logger.getLogger("CommandsAPI-optin");
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        Updater.setLogger(logger);
    }

    @AfterEach
    void tearDown() {
        logger.removeHandler(handler);
        Updater.setLogger(Logger.getLogger("CommandsAPI"));
        System.clearProperty(UpdaterSettings.PROPERTY);
    }

    @Test
    void creatingAManager_makesNoUpdateCheck() {
        try (MockedStatic<Updater> updater = mockStatic(Updater.class)) {
            new MockCommandManager();
            new MockCommandManager();

            updater.verifyNoInteractions();
        }
    }

    @Test
    void systemPropertyFalse_skipsAnExplicitCheck() {
        System.setProperty(UpdaterSettings.PROPERTY, "false");
        try (MockedStatic<Updater> updater = mockStatic(Updater.class)) {
            updater.when(Updater::checkUpdates).thenCallRealMethod();

            Updater.checkUpdates();

            updater.verify(Updater::fetchLatestVersionAsync, org.mockito.Mockito.never());
        }
    }

    @Test
    void failedCheck_announcesNothing() {
        assertTrue(runCheck("5.3.1", null).isEmpty());
    }

    @Test
    void newerLocalVersion_isNotAnnouncedAsOutdated() {
        assertTrue(runCheck("5.4.0-SNAPSHOT", "5.3.1").stream().noneMatch(r -> r.getLevel() == Level.WARNING));
    }

    @Test
    void commitBuild_announcesNothing() {
        assertTrue(runCheck("d64670c", "5.3.1").isEmpty());
    }

    @Test
    void olderVersion_isAnnouncedAsOutdated() {
        assertTrue(runCheck("5.3.1", "5.10.0").stream().anyMatch(r -> r.getLevel() == Level.WARNING));
    }

    @Test
    void versionComparison() {
        assertTrue(VersionComparator.compare("5.9.0", "5.10.0") < 0);
        assertTrue(VersionComparator.compare("5.4.0-SNAPSHOT", "5.4.0") < 0);
        assertTrue(VersionComparator.compare("5.4.0", "5.4.0-SNAPSHOT") > 0);
        assertEquals(0, VersionComparator.compare("5.4", "5.4.0"));
        assertEquals(VersionComparator.UNKNOWN, VersionComparator.compare("abc", "5.4.0"));
        assertEquals(VersionComparator.UNKNOWN, VersionComparator.compare(null, "5.4.0"));
    }

    private List<LogRecord> runCheck(String current, String latest) {
        try (MockedStatic<Updater> updater = mockStatic(Updater.class)) {
            updater.when(Updater::checkUpdates).thenCallRealMethod();
            updater.when(Updater::getVersion).thenReturn(current);
            updater.when(Updater::fetchLatestVersionAsync).thenReturn(CompletableFuture.completedFuture(latest));

            Updater.checkUpdates();
        }
        return records;
    }
}
