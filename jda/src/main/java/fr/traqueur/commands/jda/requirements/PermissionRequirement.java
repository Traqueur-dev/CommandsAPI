package fr.traqueur.commands.jda.requirements;

import fr.traqueur.commands.api.requirements.Requirement;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Arrays;
import java.util.Collection;

/**
 * Requirement that checks if the user has specific Discord permissions.
 */
public class PermissionRequirement implements Requirement<SlashCommandInteractionEvent> {

    private final Collection<Permission> permissions;
    private final String errorMessage;

    /**
     * Constructor for PermissionRequirement.
     *
     * @param permissions The permissions required.
     */
    public PermissionRequirement(Permission... permissions) {
        this(null, permissions);
    }

    /**
     * Constructor for PermissionRequirement with custom error message.
     *
     * @param errorMessage Custom error message.
     * @param permissions  The permissions required.
     */
    public PermissionRequirement(String errorMessage, Permission... permissions) {
        this.permissions = Arrays.asList(permissions);
        this.errorMessage = errorMessage != null ? errorMessage :
                "You need the following permissions: " + formatPermissions();
    }

    @Override
    public boolean check(SlashCommandInteractionEvent event) {
        if (event.getMember() == null) {
            return false;
        }
        return event.getMember().hasPermission(permissions);
    }

    @Override
    public String errorMessage() {
        return errorMessage;
    }

    private String formatPermissions() {
        return permissions.stream()
                .map(Permission::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}