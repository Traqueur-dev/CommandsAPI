package fr.traqueur.commands.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommand {

    String parent();

    String name();

    String description() default "";

    String permission() default "";

    String usage() default "";

    String[] aliases() default {};

}
