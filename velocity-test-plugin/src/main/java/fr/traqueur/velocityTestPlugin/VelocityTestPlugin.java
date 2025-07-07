package fr.traqueur.velocityTestPlugin;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.traqueur.commands.velocity.CommandManager;
import org.slf4j.Logger;

@Plugin(
    id = "velocity-test-plugin",
    name = "velocity-test-plugin",
    version = BuildConstants.VERSION
    ,authors = {"Traqueur_"}
)
public class VelocityTestPlugin {

    @Inject private Logger logger;
    @Inject private ProxyServer server;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager<VelocityTestPlugin> commandManager = new CommandManager<>(this, server, java.util.logging.Logger.getLogger(ProxyServer.class.getName()));
        commandManager.setDebug(true);
        commandManager.registerCommand(new TestCommand(this));
    }
}
