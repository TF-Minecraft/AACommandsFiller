package net.tfminecraft.aacommandsfiller.handlers;

import net.tfminecraft.aacommandsfiller.config.ConfigHelper;
import net.tfminecraft.aacommandsfiller.validators.PermissionValidator;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabCompleteHandlerTest {

    private static final File BUNDLED_CONFIG = new File("src/main/resources/config.yml");

    private TabCompleteHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        handler = handlerFor(BUNDLED_CONFIG);
    }

    @Test
    void helperTrackMembersSeeTheStepTheyCanTake() {
        CommandSender onDuty = sender("helper.demote");
        assertTrue(complete(onDuty, "").contains("helper"));
        assertEquals(List.of("demote"), complete(onDuty, "helper", ""));

        CommandSender offDuty = sender("helper.promote");
        assertEquals(List.of("promote"), complete(offDuty, "helper", ""));
        assertFalse(complete(offDuty, "").contains("helper+"));

        CommandSender seniorHelper = sender("helper+.promote");
        assertTrue(complete(seniorHelper, "").contains("helper+"));
        assertEquals(List.of("promote"), complete(seniorHelper, "helper+", ""));
    }

    @Test
    void playersDoNotSeeStaffCommands() {
        List<String> suggestions = complete(sender(), "");

        assertFalse(suggestions.contains("helper"));
        assertFalse(suggestions.contains("helper+"));
        assertFalse(suggestions.contains("ban"));
        assertTrue(suggestions.containsAll(List.of("tips", "pack", "date", "map")));
    }

    @Test
    void helpersSeeTheBanCommand() {
        CommandSender helper = sender("tfmc.helper");

        assertTrue(complete(helper, "").contains("ban"));
        assertEquals(List.of("<playername>"), complete(helper, "ban", ""));
        assertEquals(List.of("<reason>"), complete(helper, "ban", "Steve", ""));
    }

    @Test
    void parentListSurvivesDottedChildKeys(@TempDir Path dir) throws Exception {
        File config = dir.resolve("config.yml").toFile();
        Files.writeString(config.toPath(), """
                base-command: tfmc
                commands:
                  helper:
                    promote: {}
                permissions:
                  helper: [a, b]
                  helper.promote: c
                """);
        TabCompleteHandler custom = handlerFor(config);

        assertTrue(complete(custom, sender("b"), "").contains("helper"));
        assertEquals(List.of(), complete(custom, sender("b"), "helper", ""));
        assertEquals(List.of("promote"), complete(custom, sender("b", "c"), "helper", ""));
    }

    private static TabCompleteHandler handlerFor(File configFile) throws Exception {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ConfigHelper configHelper = new ConfigHelper(config, ConfigHelper.loadPermissions(configFile));
        return new TabCompleteHandler(configHelper, new PermissionValidator(configHelper), "tfmc");
    }

    private List<String> complete(CommandSender sender, String... args) {
        return complete(handler, sender, args);
    }

    private static List<String> complete(TabCompleteHandler handler, CommandSender sender, String... args) {
        return handler.handleTabComplete(sender, "tfmc", args);
    }

    private static CommandSender sender(String... permissions) {
        Set<String> granted = Set.of(permissions);
        return (CommandSender) Proxy.newProxyInstance(
                CommandSender.class.getClassLoader(),
                new Class<?>[] {CommandSender.class},
                (proxy, method, methodArgs) -> {
                    if (method.getName().equals("hasPermission") && methodArgs[0] instanceof String node) {
                        return granted.contains(node);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
