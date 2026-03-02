package tfmc.justin.handlers;

import org.bukkit.command.CommandSender;
import tfmc.justin.config.ConfigHelper;
import tfmc.justin.validators.PermissionValidator;

import java.util.ArrayList;
import java.util.List;

// ====================================
// Handles tab-completion logic
// Filters available subcommands by permission and partial input
// ====================================
public class TabCompleteHandler {
    
    private final ConfigHelper configHelper;
    private final PermissionValidator permissionValidator;
    private final String baseCommand;
    
    public TabCompleteHandler(ConfigHelper configHelper, PermissionValidator permissionValidator, String baseCommand) {
        this.configHelper = configHelper;
        this.permissionValidator = permissionValidator;
        this.baseCommand = baseCommand;
    }
    
    // ====================================
    // Handle tab-completion
    // Returns list of available subcommands filtered by:
    // - What exists in config
    // - What the sender has permission to use
    // - What matches the current partial input
    // Supports placeholder patterns like <number>, <playername>, etc.
    // ====================================
    public List<String> handleTabComplete(CommandSender sender, String commandName, String[] args) {
        if (!commandName.equalsIgnoreCase(baseCommand)) return new ArrayList<>();

        // Build path from completed args
        List<String> pathSoFar = new ArrayList<>();

        for (int i = 0; i < args.length - 1; i++) {
            String arg = args[i];
            
            // Check if this arg matches a placeholder pattern
            List<String> subCommands = configHelper.getSubCommands(pathSoFar.toArray(new String[0]));
            String matchedPlaceholder = findMatchingPlaceholder(arg, subCommands);
            
            if (matchedPlaceholder != null) {
                // Use the placeholder name instead of the literal value
                pathSoFar.add(matchedPlaceholder);
            } else {
                // Use the literal value
                pathSoFar.add(arg.toLowerCase());
            }
        }

        // Get all subcommands at current level
        List<String> possibleSubCommands = configHelper.getSubCommands(pathSoFar.toArray(new String[0]));
        
        // Filter by permission. Remove the commands sender cant use
        possibleSubCommands.removeIf(subCommand -> {
            List<String> fullPath = new ArrayList<>(pathSoFar);
            fullPath.add(subCommand);
            
            return !permissionValidator.hasPermission(sender, fullPath.toArray(new String[0]));
        });
        
        // Filter by current input. Show only matching completions
        // Dont filter placeholders if current input could match them
        possibleSubCommands.removeIf(subCommand -> {

            // If its a placeholder, show it if theres little/no input
            if (isPlaceholder(subCommand)) {
                return false; // Always show placeholders
            }

            // Regular commands must match the current input
            return !subCommand.toLowerCase().startsWith(args[args.length - 1].toLowerCase());
        });

        return possibleSubCommands;
    }
    
    // ====================================
    // Check if a subcommand is a placeholder (surrounded by < >)
    // ====================================
    private boolean isPlaceholder(String subCommand) {
        return subCommand.startsWith("<") && subCommand.endsWith(">");
    }
    
    // ====================================
    // Find which placeholder matches the given argument
    // Returns the placeholder name if matched, null otherwise
    // ====================================
    private String findMatchingPlaceholder(String arg, List<String> availableSubCommands) {
        for (String subCommand : availableSubCommands) {
            if (isPlaceholder(subCommand)) {
                if (matchesPlaceholder(arg, subCommand)) {
                    return subCommand;
                }
            }
        }
        return null;
    }
    
    // ====================================
    // Check if an argument matches a placeholder pattern
    // Supports: <number>, <playername>, <reason>, <+/-><modifier>, etc.
    // ====================================
    private boolean matchesPlaceholder(String arg, String placeholder) {
        String lowerPlaceholder = placeholder.toLowerCase();
        
        // <number> pattern. Matches integers (positive or negative)
        if (lowerPlaceholder.contains("number")) {
            try {
                Integer.parseInt(arg);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        // <+/-><modifier> pattern. Matches +5, -3, etc.
        if (lowerPlaceholder.contains("+/-") || lowerPlaceholder.contains("modifier")) {
            if (arg.length() >= 2 && (arg.startsWith("+") || arg.startsWith("-"))) {
                try {
                    Integer.parseInt(arg.substring(1));
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return false;
        }
        
        // <playername>, <player>, <name>. Matches any non-empty string
        if (lowerPlaceholder.contains("player") || lowerPlaceholder.contains("name")) {
            return arg.length() > 0;
        }
        
        // <reason>, <message>, <text>. Matches any non-empty string
        if (lowerPlaceholder.contains("reason") || lowerPlaceholder.contains("message") || lowerPlaceholder.contains("text")) {
            return arg.length() > 0;
        }
        
        // <amount> pattern. Matches numbers
        if (lowerPlaceholder.contains("amount")) {
            try {
                Integer.parseInt(arg);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        // Default. Match any non-empty string for other placeholders
        return arg.length() > 0;
    }
}
