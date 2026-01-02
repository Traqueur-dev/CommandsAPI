package fr.traqueur.testplugin;

import fr.traqueur.commands.annotations.AnnotationCommandProcessor;
import fr.traqueur.commands.spigot.CommandManager;
import fr.traqueur.testplugin.annoted.*;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        CommandManager<TestPlugin> commandManager = new CommandManager<>(this);
        commandManager.setDebug(true);

        // Create annotation processor
        AnnotationCommandProcessor<TestPlugin, CommandSender> annotationProcessor =
                new AnnotationCommandProcessor<>(commandManager);

        // Register annotated commands
        getLogger().info("Registering annotated commands...");
        annotationProcessor.register(new SimpleAnnotatedCommands());
        annotationProcessor.register(new OptionalArgsCommands());
        annotationProcessor.register(new TabCompleteCommands());
        annotationProcessor.register(new HierarchicalCommands());

        // Register traditional commands
        commandManager.registerCommand(new TestCommand(this));

        getLogger().info("All commands registered successfully!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
