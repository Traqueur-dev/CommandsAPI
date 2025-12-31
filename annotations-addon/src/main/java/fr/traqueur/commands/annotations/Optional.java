package fr.traqueur.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an {@link Arg} parameter as optional.
 * 
 * <p>Optional parameters will be null if not provided by the user.
 * Use wrapper types (Integer, Boolean, etc.) instead of primitives
 * to allow null values.</p>
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @Command(name = "heal")
 * public void heal(Player sender, @Arg("target") @Optional Player target) {
 *     Player toHeal = (target != null) ? target : sender;
 *     toHeal.setHealth(20);
 * }
 * }</pre>
 * 
 * @since 5.0.0
 * @see Arg
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Optional {
}