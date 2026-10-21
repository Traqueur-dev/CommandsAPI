package fr.traqueur.papertest;

import fr.traqueur.commands.api.arguments.Infinite;
import fr.traqueur.commands.adventure.ComponentMessageFormatter;
import fr.traqueur.commands.paper.CommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Exercises the Paper platform on a real server: typed arguments, a command added after startup,
 * a command removed at runtime, and component messages.
 * <p>
 * From the console: {@code give 3}, {@code give abc} (refused by Brigadier), {@code pt add late},
 * {@code late}, {@code pt remove give}, {@code give 3}, {@code minecraft:reload}, {@code late}.
 * </p>
 */
public final class PaperTestPlugin extends JavaPlugin {

    private CommandManager<PaperTestPlugin> manager;

    @Override
    public void onEnable() {
        manager = new CommandManager<>(this);
        manager.setComponentFormatter(ComponentMessageFormatter.miniMessage(
                key -> "<gold>[PT]</gold> <click:run_command:'/pt'>" + key + "</click> <arg><usage><requirement>"));

        manager.command("give")
                .description("Give an amount")
                .arg("amount", Integer.class)
                .executor((sender, args) -> getLogger().info("give ran with " + args.<Integer>get("amount")))
                .register();

        manager.command("echo")
                .arg("loud", Boolean.class)
                .arg("text", Infinite.class)
                .executor((sender, args) -> getLogger().info("echo " + args.get("loud") + " " + args.get("text")))
                .register();

        manager.command("pt")
                .optionalArg("action", String.class)
                .optionalArg("label", String.class)
                .executor((sender, args) -> control(sender, args.<String>getOptional("action").orElse(""),
                        args.<String>getOptional("label").orElse("")))
                .register();
    }

    private void control(CommandSender sender, String action, String label) {
        switch (action) {
            case "add" -> manager.command(label)
                    .executor((s, a) -> getLogger().info(label + " ran"))
                    .register();
            case "remove" -> manager.unregisterCommand(label);
            case "removeall" -> manager.unregisterAll();
            default -> getLogger().info("pt add|remove|removeall <label>");
        }
        getLogger().info("pt " + action + " " + label + " done");
    }

    @Override
    public void onDisable() {
        manager.unregisterAll();
    }
}
