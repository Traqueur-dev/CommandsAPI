package fr.traqueur.commands.api.resolver;

/**
 * Resolves sender types for annotated commands.
 * 
 * <p>Each platform (Bukkit, JDA, etc.) provides its own implementation
 * to handle sender type resolution from method parameters.</p>
 * 
 * <p>Example for Bukkit:</p>
 * <ul>
 *   <li>{@code CommandSender} → the raw sender</li>
 *   <li>{@code Player} → cast to Player (gameOnly = true)</li>
 *   <li>{@code ConsoleCommandSender} → cast to Console</li>
 * </ul>
 * 
 * <p>Example for JDA:</p>
 * <ul>
 *   <li>{@code SlashCommandInteractionEvent} → the raw event</li>
 *   <li>{@code User} → event.getUser()</li>
 *   <li>{@code Member} → event.getMember() (gameOnly = true, requires guild)</li>
 * </ul>
 * 
 * @param <S> the base sender type for the platform
 * @since 5.0.0
 */
public interface SenderResolver<S> {
    
    /**
     * Checks if this resolver can handle the given parameter type.
     * 
     * @param type the parameter type from the method signature
     * @return true if this resolver can resolve the type
     */
    boolean canResolve(Class<?> type);
    
    /**
     * Resolves the sender to the requested type.
     * 
     * @param sender the original sender from the command execution
     * @param type the requested type from the method parameter
     * @return the resolved object, or null if resolution fails
     */
    Object resolve(S sender, Class<?> type);
    
    /**
     * Checks if the given type requires a "game" context.
     * 
     * <p>For Bukkit, this means the sender must be a Player.
     * For JDA, this means the command must be executed in a guild (Member).</p>
     * 
     * @param type the parameter type
     * @return true if this type requires game-only context
     */
    boolean isGameOnly(Class<?> type);
}