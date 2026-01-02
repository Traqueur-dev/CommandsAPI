package fr.traqueur.commands.annotations.commands;

import fr.traqueur.commands.annotations.Command;
import fr.traqueur.commands.annotations.CommandContainer;
import fr.traqueur.commands.test.mocks.*;

import java.util.ArrayList;
import java.util.List;

@CommandContainer
public class HierarchicalTestCommands {

    public final List<String> executedCommands = new ArrayList<>();

    @Command(name = "admin", description = "Admin commands")
    public void admin(MockSender sender) {
        executedCommands.add("admin");
    }

    @Command(name = "admin.reload", description = "Reload configuration", permission = "admin.reload")
    public void adminReload(MockSender sender) {
        executedCommands.add("admin.reload");
    }

    @Command(name = "admin.info", description = "Show server info")
    public void adminInfo(MockSender sender) {
        executedCommands.add("admin.info");
    }

    @Command(name = "admin.reload.config", description = "Reload config file")
    public void adminReloadConfig(MockSender sender) {
        executedCommands.add("admin.reload.config");
    }

    @Command(name = "admin.reload.plugins", description = "Reload plugins")
    public void adminReloadPlugins(MockSender sender) {
        executedCommands.add("admin.reload.plugins");
    }
}