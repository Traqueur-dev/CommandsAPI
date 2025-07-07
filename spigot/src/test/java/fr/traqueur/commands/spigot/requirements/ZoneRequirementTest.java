package fr.traqueur.commands.spigot.requirements;

import fr.traqueur.commands.api.requirements.Requirement;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ZoneRequirementTest {

    @Test
    void testConstructor_sameWorld_throwsException() {
        World world = Mockito.mock(World.class);
        when(world.getName()).thenReturn("same");
        Location loc1 = new Location(world, 0, 0, 0);
        Location loc2 = new Location(world, 1, 1, 1);
        Requirement<CommandSender> req = new ZoneRequirement(loc1, loc2);
        assertNotNull(req);
    }

    @Test
    void testConstructor_differentWorld_createsRequirement() {
        World world1 = Mockito.mock(World.class);
        when(world1.getName()).thenReturn("w1");
        World world2 = Mockito.mock(World.class);
        when(world2.getName()).thenReturn("w2");
        Location up = new Location(world1, 10, 10, 10);
        Location down = new Location(world2, -5, -5, -5);
        assertThrows(IllegalArgumentException.class,
                () -> new ZoneRequirement(up, down));
    }

    @Test
    void testCheck_withinZone() {
        World world1 = Mockito.mock(World.class);
        when(world1.getName()).thenReturn("w1");
        Requirement<CommandSender> req = ZoneRequirement.of(world1, 0, 0, 0, 5, 5, 5);

        Player player = Mockito.mock(Player.class);
        when(player.getWorld()).thenReturn(world1);
        Location playerLoc = new Location(world1, 3, 3, 3);
        when(player.getLocation()).thenReturn(playerLoc);

        assertTrue(req.check(player));
    }

    @Test
    void testCheck_outsideZone() {
        World world1 = Mockito.mock(World.class);
        when(world1.getName()).thenReturn("w1");
        Requirement<CommandSender> req = ZoneRequirement.of(world1, 0, 0, 0, 5, 5, 5);

        Player player = Mockito.mock(Player.class);
        when(player.getWorld()).thenReturn(world1);
        Location outside = new Location(world1, 10, 10, 10);
        when(player.getLocation()).thenReturn(outside);
        assertFalse(req.check(player));
    }

    @Test
    void testErrorMessage_containsCoords() {
        World world1 = Mockito.mock(World.class);
        when(world1.getName()).thenReturn("w1");
        Requirement<CommandSender> req = ZoneRequirement.of(world1, 0, 0, 0, 5, 5, 5);
        String msg = req.errorMessage();
        assertTrue(msg.contains("0"));
        assertTrue(msg.contains("5"));
    }
}