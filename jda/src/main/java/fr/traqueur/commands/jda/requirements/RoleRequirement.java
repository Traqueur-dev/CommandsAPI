package fr.traqueur.commands.jda.requirements;

import fr.traqueur.commands.api.requirements.Requirement;
import fr.traqueur.commands.jda.JDAInteractionContext;
import net.dv8tion.jda.api.entities.Member;

import java.util.Arrays;
import java.util.Collection;

/**
 * Requirement that checks if the user has specific Discord roles.
 */
public class RoleRequirement implements Requirement<JDAInteractionContext> {

    private final Collection<Long> roleIds;
    private final String errorMessage;
    private final boolean requireAll;

    /**
     * Constructor for RoleRequirement.
     * Requires at least one of the specified roles.
     *
     * @param roleIds The role IDs required.
     */
    public RoleRequirement(Long... roleIds) {
        this(false, null, roleIds);
    }

    /**
     * Constructor for RoleRequirement with custom error message.
     *
     * @param requireAll   Whether all roles are required (true) or just one (false).
     * @param errorMessage Custom error message.
     * @param roleIds      The role IDs required.
     */
    public RoleRequirement(boolean requireAll, String errorMessage, Long... roleIds) {
        this.roleIds = Arrays.asList(roleIds);
        this.requireAll = requireAll;
        this.errorMessage = errorMessage != null ? errorMessage :
                "You don't have the required role" + (requireAll ? "s" : "") + " to use this command.";
    }

    @Override
    public boolean check(JDAInteractionContext context) {
        Member member = context.getMember();
        if (member == null) {
            return false;
        }

        if (requireAll) {
            // Must have all roles
            return roleIds.stream()
                    .allMatch(roleId -> member.getRoles().stream()
                            .anyMatch(role -> role.getIdLong() == roleId));
        } else {
            // Must have at least one role
            return member.getRoles().stream()
                    .anyMatch(role -> roleIds.contains(role.getIdLong()));
        }
    }

    @Override
    public String errorMessage() {
        return errorMessage;
    }
}
