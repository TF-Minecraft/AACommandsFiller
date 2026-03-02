package tfmc.justin.validators;

import org.bukkit.command.CommandSender;
import tfmc.justin.config.ConfigHelper;

import java.util.List;

// ====================================
// Handles permission validation for command paths
// Checks if a sender has the required permissions to use a command
// ====================================
public class PermissionValidator {
    
    private final ConfigHelper configHelper;
    
    public PermissionValidator(ConfigHelper configHelper) {
        this.configHelper = configHelper;
    }
    
    // ====================================
    // Check if a sender has permission to use a command
    // Returns true if no permission is required
    // Uses OR logic: player needs ANY of the listed permissions
    // ====================================
    public boolean hasPermission(CommandSender sender, String... pathParts) {
        List<String> perms = configHelper.getPermissions(pathParts);
        
        // No permissions required = public command
        if (perms.isEmpty()) {
            return true;
        }
        
        // Check if sender has ANY of the permissions (OR logic)
        for (String perm : perms) {
            if (sender.hasPermission(perm)) {
                return true;
            }
        }
        
        return false;
    }
}
