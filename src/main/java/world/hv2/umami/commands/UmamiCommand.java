package world.hv2.umami.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import world.hv2.umami.UmamiPlugin;
import world.hv2.umami.config.ConfigManager;
import world.hv2.umami.services.UmamiService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command handler for the Umami plugin.
 */
public class UmamiCommand implements CommandExecutor, TabCompleter {
    
    private final UmamiPlugin plugin;
    private final ConfigManager configManager;
    private final UmamiService umamiService;
    
    private final List<String> subcommands = Arrays.asList("reload", "status", "test", "online", "version");

    public UmamiCommand(UmamiPlugin plugin, ConfigManager configManager, UmamiService umamiService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.umamiService = umamiService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "reload":
                return handleReload(sender);
            case "status":
                return handleStatus(sender);
            case "test":
                return handleTest(sender);
            case "online":
                return handleOnline(sender);
            case "version":
                return handleVersion(sender);
            default:
                showHelp(sender);
                return true;
        }
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Umami Analytics Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/umami reload", NamedTextColor.YELLOW)
                .append(Component.text(" - Reload the configuration", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/umami status", NamedTextColor.YELLOW)
                .append(Component.text(" - Show plugin status", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/umami test", NamedTextColor.YELLOW)
                .append(Component.text(" - Send a test event to Umami", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/umami online", NamedTextColor.YELLOW)
                .append(Component.text(" - Show current online players", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/umami version", NamedTextColor.YELLOW)
                .append(Component.text(" - Show plugin version", NamedTextColor.GRAY)));
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("umami.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        try {
            configManager.reloadConfig();
            sender.sendMessage(Component.text("Umami configuration reloaded successfully!", NamedTextColor.GREEN));
            
            // Send reload event
            umamiService.sendEvent("minecraft_plugin_reload", null);
            
        } catch (Exception e) {
            sender.sendMessage(Component.text("Failed to reload configuration: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
        }
        
        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        if (!sender.hasPermission("umami.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text("=== Umami Plugin Status ===", NamedTextColor.GOLD));
        
        // Configuration status
        boolean configured = umamiService.isConfigured();
        sender.sendMessage(Component.text("Configuration: ", NamedTextColor.AQUA)
                .append(Component.text(configured ? "✓ Configured" : "✗ Not Configured", 
                        configured ? NamedTextColor.GREEN : NamedTextColor.RED)));
        
        // API endpoint
        sender.sendMessage(Component.text("API Endpoint: ", NamedTextColor.AQUA)
                .append(Component.text(configManager.getApiEndpoint(), NamedTextColor.WHITE)));
        
        // Website ID
        String websiteId = configManager.getWebsiteId();
        if (websiteId.equals("your-website-id")) {
            sender.sendMessage(Component.text("Website ID: ", NamedTextColor.AQUA)
                    .append(Component.text("Not configured", NamedTextColor.RED)));
        } else {
            sender.sendMessage(Component.text("Website ID: ", NamedTextColor.AQUA)
                    .append(Component.text(websiteId, NamedTextColor.WHITE)));
        }
        
        // Tracking status
        sender.sendMessage(Component.text("Tracking Status:", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("  Player Login: ", NamedTextColor.GRAY)
                .append(Component.text(configManager.isPlayerLoginTracked() ? "✓" : "✗", 
                        configManager.isPlayerLoginTracked() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        sender.sendMessage(Component.text("  Player Logout: ", NamedTextColor.GRAY)
                .append(Component.text(configManager.isPlayerLogoutTracked() ? "✓" : "✗", 
                        configManager.isPlayerLogoutTracked() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        sender.sendMessage(Component.text("  Player Chat: ", NamedTextColor.GRAY)
                .append(Component.text(configManager.isPlayerChatTracked() ? "✓" : "✗", 
                        configManager.isPlayerChatTracked() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        sender.sendMessage(Component.text("  Crafting: ", NamedTextColor.GRAY)
                .append(Component.text(configManager.isCraftingTracked() ? "✓" : "✗", 
                        configManager.isCraftingTracked() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        
        return true;
    }

    private boolean handleTest(CommandSender sender) {
        if (!sender.hasPermission("umami.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (!umamiService.isConfigured()) {
            sender.sendMessage(Component.text("Umami is not configured. Please check your config.yml file.", NamedTextColor.RED));
            return true;
        }

        sender.sendMessage(Component.text("Sending test event to Umami...", NamedTextColor.YELLOW));
        
        CompletableFuture<Boolean> testFuture = umamiService.sendTestEvent();
        testFuture.whenComplete((success, throwable) -> {
            if (success) {
                sender.sendMessage(Component.text("✓ Test event sent successfully!", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("✗ Test event failed. Check server logs for details.", NamedTextColor.RED));
            }
        });
        
        return true;
    }

    private boolean handleOnline(CommandSender sender) {
        int playerCount = Bukkit.getOnlinePlayers().size();
        sender.sendMessage(Component.text("Online Players: ", NamedTextColor.AQUA)
                .append(Component.text(playerCount, NamedTextColor.WHITE)));
        
        if (playerCount > 0) {
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            sender.sendMessage(Component.text("Players: ", NamedTextColor.GRAY)
                    .append(Component.text(String.join(", ", playerNames), NamedTextColor.WHITE)));
        }
        
        return true;
    }

    private boolean handleVersion(CommandSender sender) {
        sender.sendMessage(Component.text("=== Umami Analytics Plugin ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Version: ", NamedTextColor.AQUA)
                .append(Component.text(plugin.getDescription().getVersion(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Author: ", NamedTextColor.AQUA)
                .append(Component.text("Carmelo Santana", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Website: ", NamedTextColor.AQUA)
                .append(Component.text("https://github.com/carmelosantana/minecraft-umami", NamedTextColor.WHITE)));
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String partial = args[0].toLowerCase();
            
            for (String subcommand : subcommands) {
                if (subcommand.startsWith(partial)) {
                    completions.add(subcommand);
                }
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
}
