package fr.traqueur.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a command on a method within a {@link CommandContainer}.
 * 
 * <p>The method's first parameter should be the sender type (resolved via
 * the platform's SenderResolver). Additional parameters should be annotated
 * with {@link Arg}.</p>
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @Command(name = "heal", permission = "admin.heal", description = "Heal a player")
 * public void heal(Player sender, @Arg("target") @Optional Player target) {
 *     Player toHeal = (target != null) ? target : sender;
 *     toHeal.setHealth(20);
 * }
 * }</pre>
 * 
 * @since 5.0.0
 * @see CommandContainer
 * @see Arg
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    
    /**
     * The command name (without /).
     * 
     * @return the command name
     */
    String name();
    
    /**
     * The permission required to execute this command.
     * Empty string means no permission required.
     * 
     * @return the permission node
     */
    String permission() default "";
    
    /**
     * The command description.
     * 
     * @return the description
     */
    String description() default "";
    
    /**
     * The usage string displayed on incorrect usage.
     * If empty, auto-generated from arguments.
     * 
     * @return the usage string
     */
    String usage() default "";
}