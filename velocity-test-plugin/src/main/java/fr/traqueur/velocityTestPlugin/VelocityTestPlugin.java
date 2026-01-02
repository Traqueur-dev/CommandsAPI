package fr.traqueur.velocityTestPlugin;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.traqueur.commands.annotations.AnnotationCommandProcessor;
import fr.traqueur.commands.velocity.CommandManager;
import fr.traqueur.velocityTestPlugin.annoted.*;
import org.slf4j.Logger;

@Plugin(
        id = "velocity-test-plugin",
        name = "velocity-test-plugin",
        version = BuildConstants.VERSION
        , authors = {"Traqueur_"}
)
public class VelocityTestPlugin {

    @Inject
    private Logger logger;
    @Inject
    private ProxyServer server;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager<VelocityTestPlugin> commandManager = new CommandManager<>(this, server, java.util.logging.Logger.getLogger(ProxyServer.class.getName()));
        commandManager.setDebug(true);

        // Create annotation processor
        AnnotationCommandProcessor<VelocityTestPlugin, CommandSource> annotationProcessor =
                new AnnotationCommandProcessor<>(commandManager);

        // Register annotated commands
        logger.info("Registering annotated commands...");
        annotationProcessor.register(new SimpleAnnotatedCommands());
        annotationProcessor.register(new OptionalArgsCommands());
        annotationProcessor.register(new TabCompleteCommands());
        annotationProcessor.register(new HierarchicalCommands());

        // Register traditional commands
        commandManager.registerCommand(new TestCommand(this));

        logger.info("All commands registered successfully!");
    }
}
