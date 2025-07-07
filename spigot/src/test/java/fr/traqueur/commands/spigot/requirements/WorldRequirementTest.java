package fr.traqueur.commands.spigot.requirements;

import fr.traqueur.commands.api.requirements.Requirement;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorldRequirementTest {
    private World mockWorld;
    private Player mockPlayer;
    private CommandSender mockSender;
    private Requirement<CommandSender> requirement;

    @BeforeEach
    void setUp() {
        mockWorld = Mockito.mock(World.class);
        when(mockWorld.getName()).thenReturn("worldA");
        when(mockWorld.getUID()).thenReturn(java.util.UUID.randomUUID());

        mockPlayer = Mockito.mock(Player.class);
        when(mockPlayer.getWorld()).thenReturn(mockWorld);

        mockSender = Mockito.mock(CommandSender.class);
    }

    @Test
    void testCheck_playerInCorrectWorld() {
        requirement = new WorldRequirement(mockWorld);
        assertTrue(requirement.check(mockPlayer));
    }

    @Test
    void testCheck_senderNotPlayer() {
        requirement = new WorldRequirement(mockWorld);
        assertFalse(requirement.check(mockSender));
    }

    @Test
    void testErrorMessage_containsWorldName() {
        requirement = new WorldRequirement(mockWorld);
        String msg = requirement.errorMessage();
        assertTrue(msg.contains("worldA"));
    }
}
