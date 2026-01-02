package fr.traqueur.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an {@link Arg} parameter as infinite (varargs).
 * 
 * <p>An infinite argument consumes all remaining input and joins it
 * into a single string. Typically used for messages or reasons.</p>
 * 
 * <p>There can only be one infinite argument per command, and it must
 * be the last argument.</p>
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @Command(name = "broadcast")
 * public void broadcast(CommandSender sender, @Arg("message") @Infinite String message) {
 *     Bukkit.broadcastMessage(message);
 * }
 * 
 * @Command(name = "kick")
 * public void kick(Player sender, 
 *                  @Arg("player") Player target,
 *                  @Arg("reason") @Optional @Infinite String reason) {
 *     target.kick(reason != null ? reason : "You have been kicked");
 * }
 * }</pre>
 * 
 * @since 5.0.0
 * @see Arg
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Infinite {
}