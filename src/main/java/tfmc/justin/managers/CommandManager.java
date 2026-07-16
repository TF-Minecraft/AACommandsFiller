package tfmc.justin.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tfmc.justin.handlers.CommandHandler;
import tfmc.justin.handlers.TabCompleteHandler;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

// ====================================
// Manages dynamic command registration
// Handlers are swappable so /aacommandsfiller reload can rebuild them
// without re-registering the command
// ====================================
public class CommandManager {

    private final JavaPlugin plugin;
    private CommandHandler commandHandler;
    private TabCompleteHandler tabCompleteHandler;
    private Command registeredCommand;

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // ====================================
    // Swap in freshly built handlers (initial setup and reloads)
    // ====================================
    public void setHandlers(CommandHandler commandHandler, TabCompleteHandler tabCompleteHandler) {
        this.commandHandler = commandHandler;
        this.tabCompleteHandler = tabCompleteHandler;
    }

    public String getRegisteredCommandName() {
        return registeredCommand != null ? registeredCommand.getName() : null;
    }

    // ====================================
    // Register command dynamically via reflection
    // ====================================
    public void registerCommand(String baseCommand) {
        try {
            CommandMap commandMap = getCommandMap();

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
            registeredCommand = command;

            // Push the updated command tree to already-online players
            // (matters for live loads/reloads via PlugManX or /aacommandsfiller reload)
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.updateCommands();
            }

            plugin.getLogger().info("AACommandsFiller is enabled! Registered command: /" + baseCommand);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register command dynamically: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ====================================
    // Unregister the command (plugin disable or base-command change)
    // Removes the knownCommands entries so a stale command doesn't keep
    // pointing at dead handlers after a PlugManX unload
    // ====================================
    public void unregisterCommand() {
        if (registeredCommand == null) return;

        try {
            CommandMap commandMap = getCommandMap();
            registeredCommand.unregister(commandMap);

            // knownCommands is declared on SimpleCommandMap; the runtime class
            // may be a subclass (CraftCommandMap), so walk up the hierarchy
            Field knownCommandsField = findField(commandMap.getClass(), "knownCommands");
            knownCommandsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            Command toRemove = registeredCommand;
            knownCommands.values().removeIf(cmd -> cmd == toRemove);

            registeredCommand = null;

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.updateCommands();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to unregister command: " + e.getMessage());
        }
    }

    // ====================================
    // Find a field on a class or any of its superclasses
    // ====================================
    private Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // keep walking up
            }
        }
        throw new NoSuchFieldException(name);
    }

    // ====================================
    // Access the server CommandMap via reflection
    // ====================================
    private CommandMap getCommandMap() throws ReflectiveOperationException {
        Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        return (CommandMap) commandMapField.get(Bukkit.getServer());
    }
}
