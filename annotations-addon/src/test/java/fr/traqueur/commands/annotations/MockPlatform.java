package fr.traqueur.commands.annotations;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandPlatform;
import fr.traqueur.commands.api.resolver.SenderResolver;

import java.util.*;
import java.util.logging.Logger;

public class MockPlatform implements CommandPlatform<Object, MockSender> {

    private final Object plugin = new Object();
    private final Logger logger = Logger.getLogger("MockPlatform");
    private final MockSenderResolver senderResolver = new MockSenderResolver();
    private final Map<String, Command<Object, MockSender>> registeredCommands = new HashMap<>();
    private final List<String> registeredLabels = new ArrayList<>();
    
    private CommandManager<Object, MockSender> commandManager;

    @Override
    public Object getPlugin() {
        return plugin;
    }

    @Override
    public void injectManager(CommandManager<Object, MockSender> commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public boolean hasPermission(MockSender sender, String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public boolean isPlayer(MockSender sender) {
        return sender instanceof MockPlayer;
    }

    @Override
    public void sendMessage(MockSender sender, String message) {
        sender.sendMessage(message);
    }

    @Override
    public void addCommand(Command<Object, MockSender> command, String label) {
        registeredCommands.put(label, command);
        registeredLabels.add(label);
    }

    @Override
    public void removeCommand(String label, boolean subcommand) {
        registeredCommands.remove(label);
        registeredLabels.remove(label);
    }

    @Override
    public SenderResolver<MockSender> getSenderResolver() {
        return senderResolver;
    }

    // Test helpers
    public Map<String, Command<Object, MockSender>> getRegisteredCommands() {
        return registeredCommands;
    }

    public List<String> getRegisteredLabels() {
        return registeredLabels;
    }

    public boolean hasCommand(String label) {
        return registeredCommands.containsKey(label);
    }

    public Command<Object, MockSender> getCommand(String label) {
        return registeredCommands.get(label);
    }
}