package fr.traqueur.testplugin;

import fr.traqueur.commands.annotations.AnnotationsProvider;
import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.testplugin.annotations.AnnotedTestCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        CommandManager<TestPlugin> commandManager = new CommandManager<>(this);
        commandManager.setDebug(true);
        commandManager.registerCommand(new TestCommand(this));
        AnnotationsProvider<TestPlugin> annotationsProvider = new AnnotationsProvider<>(this, commandManager);
        annotationsProvider.addCommand(new AnnotedTestCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
