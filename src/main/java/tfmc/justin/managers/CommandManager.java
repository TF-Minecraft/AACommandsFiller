package tfmc.justin.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import tfmc.justin.handlers.CommandHandler;
import tfmc.justin.handlers.TabCompleteHandler;

import java.lang.reflect.Field;
import java.util.List;

// ====================================
// Manages dynamic command registration
// ====================================
public class CommandManager {
    
    private final JavaPlugin plugin;
    private final CommandHandler commandHandler;
    private final TabCompleteHandler tabCompleteHandler;
    
    public CommandManager(JavaPlugin plugin, CommandHandler commandHandler, TabCompleteHandler tabCompleteHandler) {
        this.plugin = plugin;
        this.commandHandler = commandHandler;
        this.tabCompleteHandler = tabCompleteHandler;
    }
    
    // ====================================
    // Register command dynamically via reflection
    // ====================================
    public void registerCommand(String baseCommand) {
        try {
            // Access the CommandMap via reflection
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            
            // Create command instance with delegates to handlers
            Command command = new Command(baseCommand) {

                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    return commandHandler.handleCommand(sender, this.getName(), args);
                }
                
                @Override
                public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                    return tabCompleteHandler.handleTabComplete(sender, this.getName(), args);
                }
            };
            
            // Register the command
            commandMap.register(plugin.getName(), command);
            
            plugin.getLogger().info("AACommandsFiller is enabled! Registered command: /" + baseCommand);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register command dynamically: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
