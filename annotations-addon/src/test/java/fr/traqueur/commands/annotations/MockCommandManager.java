package fr.traqueur.commands.annotations;

import fr.traqueur.commands.api.CommandManager;

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