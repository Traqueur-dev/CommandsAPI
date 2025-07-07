package fr.traqueur.commands.spigot;

import fr.traqueur.commands.api.CommandManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;

/**
 * Represents the executor of the commands.
 * @param <T> The type of the plugin that owns the executor.
 */
public class SpigotExecutor<T extends Plugin> implements CommandExecutor, org.bukkit.command.TabCompleter {

    /**
     * The plugin that owns the executor.
     */
    private final T plugin;

    /**
     * The command manager.
     */
    private final CommandManager<T, CommandSender> commandManager;

    /**
     * The constructor of the executor.
     * @param plugin The plugin that owns the executor.
     * @param commandManager The command manager.
     */
    public SpigotExecutor(T plugin, CommandManager<T, CommandSender> commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
    }

    /**
     * Parse the label of the command.
     * @param label The label of the command.
     * @return The parsed label or null if the label is not valid.
     */
    private String parseLabel(String label) {
        if(label.contains(":")) {
            String[] split = label.split(":");
            label = split[1];
            if(!split[0].equalsIgnoreCase(plugin.getName())) {
                return null;
            }
        }
        return label.toLowerCase();
    }

    /**
     * This method is called when a command is executed.
     * @param sender The sender of the command.
     * @param command The command executed.
     * @param label The label of the command.
     * @param args The arguments of the command.
     * @return If the command is executed.
     */
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!this.plugin.isEnabled()) {
            return false;
        }

        String labelLower = this.parseLabel(label);

        return this.commandManager.getInvoker().invoke(sender, labelLower, args);
    }

    /**
     * This method is called when a tab is completed.
     * @param commandSender The sender of the command.
     * @param command The command completed.
     * @param label The label of the command.
     * @param args The arguments of the command.
     * @return The list of completions.
     */
    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String label, String[] args) {
        String labelLower = this.parseLabel(label);
        if(labelLower == null) {
            return Collections.emptyList();
        }
        return this.commandManager.getInvoker().suggest(commandSender, labelLower, args);
    }

}
