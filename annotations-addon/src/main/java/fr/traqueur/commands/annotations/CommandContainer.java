package fr.traqueur.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a container for annotated commands.
 * 
 * <p>A command container holds multiple commands defined via {@link Command}
 * annotations on methods.</p>
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @CommandContainer
 * public class AdminCommands {
 *     
 *     @Command(name = "heal", permission = "admin.heal")
 *     public void heal(Player sender, @Arg("target") Player target) {
 *         target.setHealth(20);
 *     }
 *     
 *     @Command(name = "feed", permission = "admin.feed")
 *     public void feed(Player sender) {
 *         sender.setFoodLevel(20);
 *     }
 * }
 * }</pre>
 * 
 * @since 5.0.0
 * @see Command
 * @see AnnotationCommandProcessor
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandContainer {
}