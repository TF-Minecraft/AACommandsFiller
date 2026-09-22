package net.tfminecraft.aacommandsfiller.config;

import org.bukkit.configuration.ConfigurationSection;
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

        // Check the exact path first
        List<String> permissions = readPermissionsAt("permissions." + commandPath);
        if (!permissions.isEmpty()) {
            return permissions;
        }

        // Check parent paths (e.g., "helper" for "helper.demote")
        String[] parts = commandPath.split("\\.");

        for (int i = parts.length - 1; i > 0; i--) {
            String parentPath = String.join(".", java.util.Arrays.copyOfRange(parts, 0, i));
            permissions = readPermissionsAt("permissions." + parentPath);

            if (!permissions.isEmpty()) {
                return permissions;
            }
        }

        return permissions;
    }

    // ====================================
    // Read the permission entry at a config path
    // Accepts a single string or a list of strings (OR logic)
    // ====================================
    private List<String> readPermissionsAt(String configPath) {
        List<String> permissions = new ArrayList<>();
        Object permObj = config.get(configPath);

        if (permObj == null) {
            return permissions;
        }

        if (permObj instanceof List) {
            for (Object perm : (List<?>) permObj) {
                if (perm != null && !perm.toString().isEmpty()) {
                    permissions.add(perm.toString());
                }
            }
        } else {
            String permStr = permObj.toString();
            if (!permStr.isEmpty()) {
                permissions.add(permStr);
            }
        }

        return permissions;
    }
    
    // ====================================
    // Get all subcommands for a given command path
    // ====================================
    public List<String> getSubCommands(String... pathParts) {
        String path = "commands." + String.join(".", pathParts).toLowerCase();

        // Leaf entries may be scalars (e.g. "help: true") rather than
        // sections, in which case there are no subcommands
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section != null) {
            return new ArrayList<>(section.getKeys(false));
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
