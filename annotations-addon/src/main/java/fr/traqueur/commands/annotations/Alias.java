package fr.traqueur.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines aliases for a {@link Command}.
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @Command(name = "gamemode", permission = "admin.gamemode")
 * @Alias({"gm"})
 * public void gamemode(Player sender, @Arg("mode") GameMode mode) {
 *     sender.setGameMode(mode);
 * }
 * 
 * @Command(name = "heal")
 * @Alias({"h", "soin", "vida"})
 * public void heal(Player sender) {
 *     sender.setHealth(20);
 * }
 * }</pre>
 * 
 * @since 5.0.0
 * @see Command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Alias {
    
    /**
     * The aliases for the command.
     * 
     * @return array of alias names
     */
    String[] value();
}