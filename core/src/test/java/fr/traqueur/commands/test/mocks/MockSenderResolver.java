package fr.traqueur.commands.test.mocks;

import fr.traqueur.commands.api.resolver.SenderResolver;

/**
 * Mock sender resolver for testing purposes.
 */
public class MockSenderResolver implements SenderResolver<MockSender> {

    @Override
    public boolean canResolve(Class<?> type) {
        return MockSender.class.isAssignableFrom(type) || MockPlayer.class.isAssignableFrom(type);
    }

    @Override
    public Object resolve(MockSender sender, Class<?> type) {
        if (type.isInstance(sender)) {
            return type.cast(sender);
        }
        return null;
    }

    @Override
    public boolean isGameOnly(Class<?> type) {
        return MockPlayer.class.isAssignableFrom(type);
    }
}
