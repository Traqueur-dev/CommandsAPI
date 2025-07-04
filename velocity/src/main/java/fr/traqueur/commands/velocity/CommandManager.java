package fr.traqueur.commands.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.logging.Logger;

public class CommandManager<T> extends fr.traqueur.commands.api.CommandManager<T, CommandSource> {
    public CommandManager(T instance, ProxyServer server,Logger logger) {
        super(new VelocityPlatform<>(instance,server, logger));
    }
}
