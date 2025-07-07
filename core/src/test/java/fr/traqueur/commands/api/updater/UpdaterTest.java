package fr.traqueur.commands.api.updater;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.net.URL;
import java.net.URLStreamHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdaterTest {

    private Logger commandsApiLogger;
    private TestLogHandler logHandler;

    @BeforeEach
    void setUp() {
        commandsApiLogger = Logger.getLogger("CommandsAPI");
        logHandler = new TestLogHandler();
        commandsApiLogger.addHandler(logHandler);
        commandsApiLogger.setLevel(Level.ALL);
        Updater.setLogger(commandsApiLogger);
    }

    @AfterEach
    void tearDown() {
        commandsApiLogger.removeHandler(logHandler);
    }

    @Test
    void getVersion_readsFromCommandsProperties() {
        // commands.properties in test/resources contains 'version=0.0.1'
        String version = Updater.getVersion();
        assertNotNull(version);
        assertEquals("1.0.0", version);
    }

    @Test
    void testIsUpToDate_withStaticMock_equalVersions() {
        try (MockedStatic<Updater> mocks = mockStatic(Updater.class)) {
            // stub real method to call through
            mocks.when(Updater::isUpToDate).thenCallRealMethod();
            mocks.when(Updater::getVersion).thenReturn("1.0.0");
            mocks.when(Updater::fetchLatestVersion).thenReturn("1.0.0");

            assertTrue(Updater.isUpToDate());
            mocks.verify(Updater::getVersion);
            mocks.verify(Updater::fetchLatestVersion);
        }
    }

    @Test
    void testIsUpToDate_withStaticMock_differentVersions() {
        try (MockedStatic<Updater> mocks = mockStatic(Updater.class)) {
            mocks.when(Updater::isUpToDate).thenCallRealMethod();
            mocks.when(Updater::getVersion).thenReturn("1.0.0");
            mocks.when(Updater::fetchLatestVersion).thenReturn("2.0.0");

            assertFalse(Updater.isUpToDate());
        }
    }

    @Test
    void testCheckUpdates_logsWarningWhenNotUpToDate() {
        try (MockedStatic<Updater> mocks = mockStatic(Updater.class)) {
            mocks.when(Updater::checkUpdates).thenCallRealMethod();
            mocks.when(Updater::getVersion).thenReturn("1.0.0");
            mocks.when(Updater::fetchLatestVersion).thenReturn("2.0.0");

            Updater.checkUpdates();
            assertTrue(logHandler.anyMatch(Level.WARNING,
                    rec -> rec.getMessage().contains("latest version is 2.0.0")
            ));
        }
    }

    /** Captures log records for assertions */
    private static class TestLogHandler extends Handler {
        private final java.util.List<LogRecord> records = new java.util.ArrayList<>();
        @Override public void publish(LogRecord record) { records.add(record); }
        @Override public void flush() {}
        @Override public void close() {}
        boolean anyMatch(Level lvl, java.util.function.Predicate<LogRecord> p) {
            return records.stream()
                    .anyMatch(r -> r.getLevel().equals(lvl) && p.test(r));
        }
    }
}
