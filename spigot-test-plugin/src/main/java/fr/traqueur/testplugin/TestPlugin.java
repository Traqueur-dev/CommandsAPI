package fr.traqueur.testplugin;

import fr.traqueur.commands.spigot.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        CommandManager<TestPlugin> commandManager = new CommandManager<>(this);
        commandManager.setDebug(true);
        commandManager.registerCommand(new TestCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
