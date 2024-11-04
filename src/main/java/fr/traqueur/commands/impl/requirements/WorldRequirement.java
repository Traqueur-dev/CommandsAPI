package fr.traqueur.commands.impl.requirements;

import fr.traqueur.commands.api.requirements.Requirement;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The class WorldRequirement.
 * <p>
 *     This class is used to represent a world requirement.
 * </p>
 */
public class WorldRequirement implements Requirement {

    /**
     * Create a new world requirement.
     * @param name The name of the world
     * @return The world requirement
     */
    public static Requirement of(String name) {
        return new WorldRequirement(Bukkit.getWorld(name));
    }

    /**
     * The world.
     */
    private final World world;

    /**
     * Create a new world requirement.
     * @param world The world
     */
    public WorldRequirement(World world) {
        this.world = world;
    }

    @Override
    public boolean check(CommandSender sender) {
        return sender instanceof Player && this.world != null  && ((Player) sender).getWorld().getUID().equals(this.world.getUID());
    }

    @Override
    public String errorMessage() {
        return "&cSender must be in world " + this.world.getName()+ ".";
    }
}
