package tfmc.justin;

import org.bukkit.plugin.java.JavaPlugin;
import tfmc.justin.config.ConfigHelper;
import tfmc.justin.handlers.CommandHandler;
import tfmc.justin.handlers.TabCompleteHandler;
import tfmc.justin.managers.CommandManager;
import tfmc.justin.validators.PermissionValidator;

public class AACommandsFiller extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        ConfigHelper configHelper = new ConfigHelper(getConfig());
        String baseCommand = configHelper.getBaseCommand();
        
        PermissionValidator permissionValidator = new PermissionValidator(configHelper);
        
        CommandHandler commandHandler = new CommandHandler(configHelper, permissionValidator, baseCommand);
        TabCompleteHandler tabCompleteHandler = new TabCompleteHandler(configHelper, permissionValidator, baseCommand);
        
        CommandManager commandManager = new CommandManager(this, commandHandler, tabCompleteHandler);
        commandManager.registerCommand(baseCommand);
    }

    @Override
    public void onDisable() {
        getLogger().info("AACommandsFiller is disabled! (makes conditional event commands visible in chat)");
    }
}
