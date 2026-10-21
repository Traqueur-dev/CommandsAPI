package fr.traqueur.commands.spigot;

import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.models.Command;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpigotUnregisterTest {

    private SimpleCommandMap commandMap;
    private CommandManager<JavaPlugin, CommandSender> homes;
    private CommandManager<JavaPlugin, CommandSender> warps;

    @BeforeEach
    void setUp() {
        commandMap = new SimpleCommandMap(mock(Server.class));
        homes = manager("Homes");
        warps = manager("Warps");
    }

    @Test
    void unregisterAll_removesTheCommandAndItsAliasesFromTheMap() {
        Command<JavaPlugin, CommandSender> home = command("home");
        home.addAlias("h");
        homes.registerCommand(home);
        assertNotNull(commandMap.getCommand("home"));
        assertNotNull(commandMap.getCommand("h"));
        assertNotNull(commandMap.getCommand("homes:home"));

        homes.unregisterAll();

        assertNull(commandMap.getCommand("home"));
        assertNull(commandMap.getCommand("h"));
        assertNull(commandMap.getCommand("homes:home"));
        assertNull(commandMap.getCommand("homes:h"));
    }

    @Test
    void unregisterAll_leavesTheCommandsOfAnotherManager() {
        homes.registerCommand(command("home"));
        warps.registerCommand(command("warp"));

        homes.unregisterAll();

        assertNull(commandMap.getCommand("home"));
        assertNotNull(commandMap.getCommand("warp"));
        assertNotNull(commandMap.getCommand("warps:warp"));
    }

    @Test
    void unregisterAll_leavesALabelHeldByAnotherPlugin() {
        warps.registerCommand(command("spawn"));
        // Homes asks for the same label without overriding: Warps keeps it.
        homes.registerCommand(command("spawn"));
        org.bukkit.command.Command holder = commandMap.getCommand("spawn");

        homes.unregisterAll();

        assertSame(holder, commandMap.getCommand("spawn"));
    }

    private CommandManager<JavaPlugin, CommandSender> manager(String name) {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getName()).thenReturn(name);
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getAnonymousLogger());
        return new CommandManager<>(new SpigotPlatform<>(plugin, commandMap)) {
        };
    }

    private static Command<JavaPlugin, CommandSender> command(String name) {
        return new Command<>(null, name) {
            @Override
            public void execute(CommandSender sender, Arguments arguments) {
            }
        };
    }
}
