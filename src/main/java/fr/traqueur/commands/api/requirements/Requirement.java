package fr.traqueur.commands.api.requirements;

import fr.traqueur.commands.api.requirements.impl.WorldRequirement;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * The interface Requirement.
 * <p>
 *     This interface is used to represent a requirement for commandsender externaly of command execution environement.
 * </p>
 */
public interface Requirement {

    /**
     * The overworld requirement.
     */
    Requirement OVERWORLD_REQUIREMENT = new WorldRequirement(Bukkit.getWorld("world"));

    /**
     * The nether requirement.
     */
    Requirement NETHER_REQUIREMENT = new WorldRequirement(Bukkit.getWorld("world_nether"));

    /**
     * The end requirement.
     */
    Requirement END_REQUIREMENT = new WorldRequirement(Bukkit.getWorld("world_the_end"));

    /**
     * Check if the sender meet the requirement.
     * @param sender The sender
     * @return true if the sender meet the requirement, false otherwise
     */
    boolean check(CommandSender sender);

    /**
     * Get the error message if the sender doesn't meet the requirement.
     * @return The error message
     */
    String errorMessage();
}
