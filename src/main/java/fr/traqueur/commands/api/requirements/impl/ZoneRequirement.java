package fr.traqueur.commands.api.requirements.impl;

import fr.traqueur.commands.api.requirements.Requirement;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The class ZoneRequirement.
 * <p>
 *     This class is used to represent a zone requirement.
 *     The sender must be in a specific zone to meet the requirement.
 *     The zone is defined by two locations.
 * </p>
 */
public class ZoneRequirement implements Requirement {

    /**
     * Create a new zone requirement.
     * @param locationUp The first location of the zone.
     * @param locationDown The second location of the zone.
     * @return The zone requirement.
     */
    public static Requirement of(Location locationUp, Location locationDown) {
        return new ZoneRequirement(locationUp, locationDown);
    }

    /**
     * Create a new zone requirement.
     * @param world The world of the zone.
     * @param x1 The first x coordinate of the zone.
     * @param y1 The first y coordinate of the zone.
     * @param z1 The first z coordinate of the zone.
     * @param x2 The second x coordinate of the zone.
     * @param y2 The second y coordinate of the zone.
     * @param z2 The second z coordinate of the zone.
     * @return The zone requirement.
     */
    public static Requirement of(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        return new ZoneRequirement(new Location(world, x1, y1, z1), new Location(world, x2, y2, z2));
    }


    /**
     * Create a new zone requirement.
     * @param world The world of the zone.
     * @param x1 The first x coordinate of the zone.
     * @param z1 The first z coordinate of the zone.
     * @param x2 The second x coordinate of the zone.
     * @param z2 The second z coordinate of the zone.
     * @return The zone requirement.
     */
    public static Requirement of(World world, int x1, int z1, int x2, int z2) {
        return new ZoneRequirement(new Location(world, x1, world.getMaxHeight(), z1), new Location(world, x2, world.getMinHeight(), z2));
    }

    private final Location locationUp;
    private final Location locationDown;

    /**
     * Create a new zone requirement.
     * @param locationUp The first location of the zone.
     * @param locationDown The second location of the zone.
     */
    public ZoneRequirement(Location locationUp, Location locationDown) {
        if(locationUp.getWorld() == null || locationDown.getWorld() == null) {
            throw new IllegalArgumentException("The locations must not be null.");
        }
        if(locationUp.getWorld().getName().equals(locationDown.getWorld().getName())) {
            throw new IllegalArgumentException("The locations must be in the same world.");
        }

        this.locationUp = new Location(locationUp.getWorld(), Math.max(locationUp.getBlockX(), locationDown.getBlockX()), Math.max(locationUp.getBlockY(), locationDown.getBlockY()), Math.max(locationUp.getBlockZ(), locationDown.getBlockZ()));
        this.locationDown = new Location(locationDown.getWorld(), Math.min(locationUp.getBlockX(), locationDown.getBlockX()), Math.min(locationUp.getBlockY(), locationDown.getBlockY()), Math.min(locationUp.getBlockZ(), locationDown.getBlockZ()));
    }

    /**
     * Check if player is inside the zone.
     * @return true if player is inside, false otherwise.
     */
    private boolean isInside(Player player) {
        return player.getLocation().getBlockX() >= this.locationDown.getBlockX()
                && player.getLocation().getBlockX() <= this.locationUp.getBlockX()
                && player.getLocation().getBlockY() >= this.locationDown.getBlockY()
                && player.getLocation().getBlockY() <= this.locationUp.getBlockY()
                && player.getLocation().getBlockZ() >= this.locationDown.getBlockZ()
                && player.getLocation().getBlockZ() <= this.locationUp.getBlockZ();
    }

    /**
     * Get the coordinates of a location.
     * @param location The location.
     * @return The coordinates of the location.
     */
    private String getCoords(Location location) {
        return (location.getWorld() != null ? location.getWorld().getName() + ", " : "") + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

    @Override
    public boolean check(CommandSender sender) {
        return sender instanceof Player player && this.isInside(player);
    }

    @Override
    public String errorMessage() {
        return "&cSender must be in the zone between " + this.getCoords(this.locationDown) + " and " + this.getCoords(this.locationUp) + ".";
    }
}
