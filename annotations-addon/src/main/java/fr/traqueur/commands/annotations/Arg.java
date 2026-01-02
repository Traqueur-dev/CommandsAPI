package fr.traqueur.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter as a command argument.
 * 
 * <p>The argument type is inferred from the parameter type. The type must
 * have a registered converter in the CommandManager.</p>
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @Command(name = "give")
 * public void give(Player sender, 
 *                  @Arg("player") Player target,
 *                  @Arg("item") Material item,
 *                  @Arg("amount") @Optional Integer amount) {
 *     int qty = (amount != null) ? amount : 1;
 *     target.getInventory().addItem(new ItemStack(item, qty));
 * }
 * }</pre>
 * 
 * @since 5.0.0
 * @see Infinite
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Arg {
    
    /**
     * The argument name used for parsing and tab completion.
     * 
     * @return the argument name
     */
    String value();
}