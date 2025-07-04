package fr.traqueur.commands.annotations;

import fr.traqueur.commands.api.arguments.TabCompleter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Arg {
    String name();

    /**
     * use this on infinite String argument
     * @return if the argument is infinite
     */
    boolean infinite() default false;
}
