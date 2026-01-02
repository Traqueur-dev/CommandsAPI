package fr.traqueur.commands.test.mocks;

import fr.traqueur.commands.api.CommandManager;

/**
 * Mock command manager for testing purposes.
 * Simplifies test setup by providing a ready-to-use manager with mock platform.
 */
public class MockCommandManager extends CommandManager<Object, MockSender> {

    private final MockPlatform mockPlatform;

    public MockCommandManager() {
        this(new MockPlatform());
    }

    public MockCommandManager(MockPlatform platform) {
        super(platform);
        this.mockPlatform = platform;
    }

    public MockPlatform getMockPlatform() {
        return mockPlatform;
    }
}
