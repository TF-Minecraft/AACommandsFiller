package net.tfminecraft.aacommandsfiller;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import net.tfminecraft.aacommandsfiller.config.ConfigHelper;
import net.tfminecraft.aacommandsfiller.handlers.CommandHandler;
import net.tfminecraft.aacommandsfiller.handlers.TabCompleteHandler;
import net.tfminecraft.aacommandsfiller.managers.CommandManager;
import net.tfminecraft.aacommandsfiller.validators.PermissionValidator;

import java.util.ArrayList;
import java.util.List;

public class AACommandsFiller extends JavaPlugin {

    private CommandManager commandManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        commandManager = new CommandManager(this);
        ConfigHelper configHelper = buildHandlers();
        commandManager.registerCommand(configHelper.getBaseCommand());
    }

    @Override
    public void onDisable() {
        // Remove the reflection-registered command so a live unload
        // (e.g. PlugManX) doesn't leave a stale command behind
        if (commandManager != null) {
            commandManager.unregisterCommand();
        }
        getLogger().info("AACommandsFiller is disabled! (makes conditional event commands visible in chat)");
    }

    // ====================================
    // /aacommandsfiller reload
    // ====================================
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            ConfigHelper configHelper = buildHandlers();

            // If base-command changed, re-register under the new name
            String newBaseCommand = configHelper.getBaseCommand();
            if (!newBaseCommand.equalsIgnoreCase(commandManager.getRegisteredCommandName())) {
                commandManager.unregisterCommand();
                commandManager.registerCommand(newBaseCommand);
            }

            sender.sendMessage(Component.text("AACommandsFiller config reloaded.", NamedTextColor.GREEN));
            return true;
        }

        sender.sendMessage(Component.text("Usage: /" + label + " reload", NamedTextColor.RED));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && "reload".startsWith(args[0].toLowerCase())) {
            List<String> completions = new ArrayList<>();
            completions.add("reload");
            return completions;
        }
        return new ArrayList<>();
    }

    // ====================================
    // Build config, validator, and handlers, and hand them to the
    // command manager. Called on enable and on every reload.
    // ====================================
    private ConfigHelper buildHandlers() {
        ConfigHelper configHelper = new ConfigHelper(getConfig());
        String baseCommand = configHelper.getBaseCommand();

        PermissionValidator permissionValidator = new PermissionValidator(configHelper);

        commandManager.setHandlers(
                new CommandHandler(configHelper, permissionValidator, baseCommand),
                new TabCompleteHandler(configHelper, permissionValidator, baseCommand));

        return configHelper;
    }
}
