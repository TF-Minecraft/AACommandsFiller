package net.tfminecraft.aacommandsfiller.handlers;

import org.bukkit.command.CommandSender;
import net.tfminecraft.aacommandsfiller.config.ConfigHelper;
import net.tfminecraft.aacommandsfiller.validators.PermissionValidator;

import java.util.ArrayList;
import java.util.List;

// ====================================
// Handles command execution logic
// Validates command paths and permissions incrementally
// ====================================
public class CommandHandler {
    
    private final ConfigHelper configHelper;
    private final PermissionValidator permissionValidator;
    private final String baseCommand;
    
    public CommandHandler(ConfigHelper configHelper, PermissionValidator permissionValidator, String baseCommand) {
        this.configHelper = configHelper;
        this.permissionValidator = permissionValidator;
        this.baseCommand = baseCommand;
    }
    
    // ====================================
    // Handle command execution
    // Validates command path exists in config and sender has permissions
    // ====================================
    public boolean handleCommand(CommandSender sender, String commandName, String[] args) {
        if (!commandName.equalsIgnoreCase(baseCommand)) return false;
        if (args.length == 0) return true;

        // Build command path incrementally and validate each step
        List<String> pathSoFar = new ArrayList<>();
        for (String arg : args) {
            pathSoFar.add(arg.toLowerCase());
            
            // Stop if command doesn't exist
            if (!configHelper.isCommandEnabled(pathSoFar.toArray(new String[0]))) return true;
            
            // Stop if sender lacks permission
            if (!permissionValidator.hasPermission(sender, pathSoFar.toArray(new String[0]))) return true;
        }

        return true;
    }
}
