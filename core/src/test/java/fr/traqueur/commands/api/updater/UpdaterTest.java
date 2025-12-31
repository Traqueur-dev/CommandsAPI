package fr.traqueur.commands.api.updater;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

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
        String version = Updater.getVersion();
        assertNotNull(version);
        assertEquals("1.0.0", version);
    }

    @Test
    void checkUpdatesAsync_logsWarningWhenNotUpToDate() {
        try (MockedStatic<Updater> mocks = mockStatic(Updater.class)) {

            mocks.when(Updater::checkUpdates).thenCallRealMethod();
            mocks.when(Updater::getVersion).thenReturn("1.0.0");
            mocks.when(Updater::fetchLatestVersionAsync)
                    .thenReturn(CompletableFuture.completedFuture("2.0.0"));

            Updater.checkUpdates();

            assertTrue(logHandler.anyMatch(Level.WARNING,
                    r -> r.getMessage().contains("not up to date")
            ));

            assertTrue(logHandler.anyMatch(Level.WARNING,
                    r -> r.getMessage().contains("Latest: 2.0.0")
            ));
        }
    }

    @Test
    void checkUpdatesAsync_logsUpToDateWhenVersionsMatch() {
        try (MockedStatic<Updater> mocks = mockStatic(Updater.class)) {

            mocks.when(Updater::checkUpdates).thenCallRealMethod();
            mocks.when(Updater::getVersion).thenReturn("1.0.0");
            mocks.when(Updater::fetchLatestVersionAsync)
                    .thenReturn(CompletableFuture.completedFuture("1.0.0"));

            Updater.checkUpdates();

            assertTrue(logHandler.anyMatch(Level.INFO,
                    r -> r.getMessage().contains("up to date")
            ));
        }
    }

    @Test
    void checkUpdatesAsync_doesNothingWhenLatestIsNull() {
        try (MockedStatic<Updater> mocks = mockStatic(Updater.class)) {

            mocks.when(Updater::checkUpdates).thenCallRealMethod();
            mocks.when(Updater::fetchLatestVersionAsync)
                    .thenReturn(CompletableFuture.completedFuture(null));

            Updater.checkUpdates();

            assertFalse(logHandler.anyMatch(Level.WARNING, r -> true));
        }
    }

    /* ------------------------------------------------------------ */
    /* Test log handler                                             */
    /* ------------------------------------------------------------ */

    private static class TestLogHandler extends Handler {

        private final java.util.List<LogRecord> records = new java.util.ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}

        boolean anyMatch(Level level, java.util.function.Predicate<LogRecord> predicate) {
            return records.stream()
                    .anyMatch(r -> r.getLevel().equals(level) && predicate.test(r));
        }
    }
}
