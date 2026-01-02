package fr.traqueur.commands.spigot;

import fr.traqueur.commands.api.arguments.Arguments;
import fr.traqueur.commands.api.models.Command;
import fr.traqueur.commands.api.models.CommandPlatform;
import fr.traqueur.commands.spigot.arguments.PlayerArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class SpigotIntegrationTest {

    private fr.traqueur.commands.api.CommandManager<Object, CommandSender> manager;
    private MockedStatic<Bukkit> bukkitStatic;

    @BeforeEach
    void setUp() {
        manager = new fr.traqueur.commands.api.CommandManager<Object, CommandSender>(new CommandPlatform<Object, CommandSender>() {
            @Override
            public Object getPlugin() {
                return null;
            }

            @Override
            public void injectManager(fr.traqueur.commands.api.CommandManager<Object, CommandSender> cm) {
            }

            @Override
            public java.util.logging.Logger getLogger() {
                return java.util.logging.Logger.getAnonymousLogger();
            }

            @Override
            public boolean hasPermission(CommandSender sender, String permission) {
                return true;
            }

            @Override
            public boolean isPlayer(CommandSender sender) {
                return sender instanceof Player;
            }

            @Override
            public void sendMessage(CommandSender sender, String message) {
            }

            @Override
            public void addCommand(Command<Object, CommandSender> command, String label) {
            }

            @Override
            public void removeCommand(String label, boolean subcommand) {
            }

            @Override
            public fr.traqueur.commands.api.resolver.SenderResolver<CommandSender> getSenderResolver() {
                return new fr.traqueur.commands.api.resolver.SenderResolver<CommandSender>() {
                    @Override
                    public boolean canResolve(Class<?> type) {
                        return CommandSender.class.isAssignableFrom(type) || Player.class.isAssignableFrom(type);
                    }

                    @Override
                    public Object resolve(CommandSender sender, Class<?> type) {
                        if (type.isInstance(sender)) {
                            return type.cast(sender);
                        }
                        return null;
                    }

                    @Override
                    public boolean isGameOnly(Class<?> type) {
                        return Player.class.isAssignableFrom(type);
                    }
                };
            }
        }) {
        };
        manager.registerConverter(Player.class, new PlayerArgument());
        bukkitStatic = Mockito.mockStatic(Bukkit.class);
    }

    @AfterEach
    void tearDown() {
        bukkitStatic.close();
    }

    @Test
    void testFullPipeline_playerArgument() throws Exception {
        Command<Object, CommandSender> cmd = new Command<Object, CommandSender>(null, "test") {
            @Override
            public void execute(CommandSender sender, Arguments arguments) {
                Player p = arguments.get("playerName");
                assertNotNull(p);
                assertEquals("User1", p.getName());
            }
        };
        cmd.addArgs("playerName", Player.class);
        manager.registerCommand(cmd);

        Player mockPlayer = Mockito.mock(Player.class);
        Mockito.when(mockPlayer.getName()).thenReturn("User1");
        bukkitStatic.when(() -> Bukkit.getPlayer("User1")).thenReturn(mockPlayer);

        String[] argsArr = new String[]{"User1"};
        Arguments parsed = manager.parse(cmd, argsArr);
        Player p = parsed.get("playerName");
        assertSame(mockPlayer, p);
        cmd.execute(null, parsed);
    }
}
