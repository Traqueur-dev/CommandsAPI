package fr.traqueur.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Makes an argument optional, with a value used when the sender leaves it out.
 * <p>
 * The value is written as the sender would type it, and converted by the converter of the
 * parameter's type when the command runs, so {@code @Default("1") int amount} receives
 * {@code 1}, and {@code @Default("spawn") Warp warp} goes through the {@code Warp} converter.
 * On an {@code Optional} parameter, the value is wrapped: the optional is then never empty
 * unless the converter rejects the value.
 * </p>
 * <pre>{@code
 * @Command(name = "give")
 * public void give(Player sender, @Arg("item") String item, @Arg("amount") @Default("1") int amount) { ... }
 * }</pre>
 * <p>
 * As every optional argument, it must come after the required ones.
 * </p>
 *
 * @since 5.7.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Default {

    /**
     * The value used when the argument is left out, as the sender would type it.
     *
     * @return The default value.
     */
    String value();
}
