package fr.traqueur.commands.spigot.arguments;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PlayerArgumentTest {

    @Test
    void testApply_returnsPlayerWhenFound() {
        Player mockPlayer = Mockito.mock(Player.class);
        // Stub static Bukkit.getPlayer
        try (MockedStatic<Bukkit> mocked = Mockito.mockStatic(Bukkit.class)) {
            mocked.when(() -> Bukkit.getPlayer("Alice")).thenReturn(mockPlayer);

            PlayerArgument converter = new PlayerArgument();
            assertSame(mockPlayer, converter.apply("Alice"));
        }
    }

    @Test
    void testApply_returnsNullWhenNotFoundOrNullInput() {
        PlayerArgument converter = new PlayerArgument();
        // null input
        assertNull(converter.apply(null));
        // stub to return null for unknown name
        try (MockedStatic<Bukkit> mocked = Mockito.mockStatic(Bukkit.class)) {
            mocked.when(() -> Bukkit.getPlayer("Bob")).thenReturn(null);
            assertNull(converter.apply("Bob"));
        }
    }

    @Test
    void testOnCompletion_listsOnlinePlayerNames() {
        Player p1 = Mockito.mock(Player.class);
        Player p2 = Mockito.mock(Player.class);
        Mockito.when(p1.getName()).thenReturn("Alice");
        Mockito.when(p2.getName()).thenReturn("Bob");
        Set<Player> online = new HashSet<>(Arrays.asList(p1, p2));

        try (MockedStatic<Bukkit> mocked = Mockito.mockStatic(Bukkit.class)) {
            // Stub Bukkit.getOnlinePlayers to return our set
            mocked.when(Bukkit::getOnlinePlayers).thenReturn(online);

            PlayerArgument converter = new PlayerArgument();
            List<String> completions = converter.onCompletion(null, new ArrayList<>());
            assertEquals(2, completions.size());
            assertTrue(completions.contains("Alice"));
            assertTrue(completions.contains("Bob"));
        }
    }
}