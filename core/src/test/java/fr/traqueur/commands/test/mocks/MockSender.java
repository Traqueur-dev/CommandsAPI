package fr.traqueur.commands.test.mocks;

/**
 * Mock sender for testing purposes.
 * Shared across all modules for consistent testing.
 */
public interface MockSender {
    void sendMessage(String message);
    boolean hasPermission(String permission);
}
