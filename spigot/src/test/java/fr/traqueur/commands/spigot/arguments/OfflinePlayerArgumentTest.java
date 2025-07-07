package fr.traqueur.commands.spigot.arguments;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OfflinePlayerArgumentTest {

    @Test
    void testApply_returnsOfflinePlayerWhenFound() {
        OfflinePlayer mockOff = Mockito.mock(OfflinePlayer.class);
        try (MockedStatic<Bukkit> mocked = Mockito.mockStatic(Bukkit.class)) {
            mocked.when(() -> Bukkit.getOfflinePlayer("Charlie")).thenReturn(mockOff);

            OfflinePlayerArgument converter = new OfflinePlayerArgument();
            assertSame(mockOff, converter.apply("Charlie"));
        }
    }

    @Test
    void testApply_returnsNullOnNullInput() {
        OfflinePlayerArgument converter = new OfflinePlayerArgument();
        assertNull(converter.apply(null));
    }

    @Test
    void testOnCompletion_listsAllOfflinePlayerNames() {
        OfflinePlayer o1 = Mockito.mock(OfflinePlayer.class);
        OfflinePlayer o2 = Mockito.mock(OfflinePlayer.class);
        Mockito.when(o1.getName()).thenReturn("Dave");
        Mockito.when(o2.getName()).thenReturn("Eve");
        OfflinePlayer[] offs = new OfflinePlayer[]{o1, o2};

        org.bukkit.Server mockServer = Mockito.mock(org.bukkit.Server.class);
        Mockito.when(mockServer.getOfflinePlayers()).thenReturn(offs);

        try (MockedStatic<Bukkit> mocked = Mockito.mockStatic(Bukkit.class)) {
            mocked.when(Bukkit::getServer).thenReturn(mockServer);

            OfflinePlayerArgument converter = new OfflinePlayerArgument();
            List<String> completions = converter.onCompletion(Mockito.mock(CommandSender.class), Collections.emptyList());
            assertEquals(2, completions.size());
            assertTrue(completions.contains("Dave"));
            assertTrue(completions.contains("Eve"));
        }
    }
}
