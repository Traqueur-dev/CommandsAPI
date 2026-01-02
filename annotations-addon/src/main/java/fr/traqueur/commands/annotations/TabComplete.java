package fr.traqueur.commands.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a tab completer for a specific argument of a command.
 * 
 * <p>The annotated method must return {@code List<String>} and can have
 * the sender as first parameter, followed by the current input string.</p>
 * 
 * <p>Example:</p>
 * <pre>{@code
 * @Command(name = "warp")
 * public void warp(Player sender, @Arg("name") String warpName) {
 *     // teleport to warp
 * }
 * 
 * @TabComplete(command = "warp", arg = "name")
 * public List<String> completeWarpName(Player sender, String current) {
 *     return getWarps().stream()
 *         .filter(w -> w.startsWith(current.toLowerCase()))
 *         .toList();
 * }
 * }</pre>
 * 
 * <p>For subcommands, use dot notation in the command parameter:</p>
 * <pre>{@code
 * @TabComplete(command = "warp.set", arg = "name")
 * public List<String> completeWarpSetName(Player sender, String current) {
 *     // ...
 * }
 * }</pre>
 * 
 * @since 5.0.0
 * @see Command
 * @see Arg
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TabComplete {
    
    /**
     * The command name (or path for subcommands using dot notation).
     * 
     * @return the command path
     */
    String command();
    
    /**
     * The argument name to provide completions for.
     * 
     * @return the argument name
     */
    String arg();
}