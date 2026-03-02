package tfmc.justin.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

// ====================================
// Handles reading and parsing command structure from config.yml
// Provides methods to query command paths and retrieve subcommands
// ====================================
public class ConfigHelper {
    
    private final FileConfiguration config;
    
    public ConfigHelper(FileConfiguration config) {
        this.config = config;
    }
    
    // ====================================
    // Check if a command path exists in the config
    // Example: isCommandEnabled("roll", "strength") checks commands.roll.strength
    // ====================================
    public boolean isCommandEnabled(String... pathParts) {
        String path = "commands." + String.join(".", pathParts).toLowerCase();
        return config.contains(path);
    }
    
    // ====================================
    // Get the permission nodes for a command path
    // Returns empty list if no permission is set (command is public)
    // ====================================
    public List<String> getPermissions(String... pathParts) {
        String commandPath = String.join(".", pathParts).toLowerCase();
        List<String> permissions = new ArrayList<>();
        
        // Check singular permissions section first
        Object permObj = config.get("permissions." + commandPath);
        if (permObj != null) {
            if (permObj instanceof List) {

                // Multiple permissions (OR logic)
                List<?> permList = (List<?>) permObj;
                for (Object perm : permList) {
                    if (perm != null && !perm.toString().isEmpty()) {
                        permissions.add(perm.toString());
                    }
                }
            } else {

                //Singular permission
                String permStr = permObj.toString();
                if (!permStr.isEmpty()) {
                    permissions.add(permStr);
                }
            }
            
            if (!permissions.isEmpty()) {
                return permissions;
            }
        }
        
        // Check parent paths (e.g., "helper" for "helper.demote")
        String[] parts = commandPath.split("\\.");

        for (int i = parts.length - 1; i > 0; i--) {
            String parentPath = String.join(".", java.util.Arrays.copyOfRange(parts, 0, i));
            Object parentPermObj = config.get("permissions." + parentPath);

            if (parentPermObj != null) {
                if (parentPermObj instanceof List) {
                    List<?> permList = (List<?>) parentPermObj;

                    for (Object perm : permList) {
                        if (perm != null && !perm.toString().isEmpty()) {
                            permissions.add(perm.toString());
                        }
                    }

                } else {
                    String permStr = parentPermObj.toString();
                    if (!permStr.isEmpty()) {
                        permissions.add(permStr);
                    }
                }

                if (!permissions.isEmpty()) {
                    return permissions;
                }
            }
        }        
        
        return permissions;
    }
    
    // ====================================
    // Get all subcommands for a given command path
    // ====================================
    public List<String> getSubCommands(String... pathParts) {
        String path = "commands." + String.join(".", pathParts).toLowerCase();

        if (config.contains(path)) {
            return new ArrayList<>(config.getConfigurationSection(path).getKeys(false));
        }

        return new ArrayList<>();
    }
    
    // ====================================
    // Get the base command name from config
    // ====================================
    public String getBaseCommand() {
        return config.getString("base-command", "tfmc");
    }
}
