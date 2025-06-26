package world.hv2.umami;

import org.bukkit.plugin.java.JavaPlugin;
import world.hv2.umami.commands.UmamiCommand;
import world.hv2.umami.config.ConfigManager;
import world.hv2.umami.listeners.PlayerEventListener;
import world.hv2.umami.services.UmamiService;

/**
 * Main plugin class for the Umami Analytics plugin.
 * Tracks player activity and sends analytics data to Umami.
 */
public class UmamiPlugin extends JavaPlugin {
    
    private ConfigManager configManager;
    private UmamiService umamiService;
    private PlayerEventListener playerEventListener;

    @Override
    public void onEnable() {
        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize Umami service
        umamiService = new UmamiService(configManager);

        // Register event listeners
        playerEventListener = new PlayerEventListener(this, umamiService);
        getServer().getPluginManager().registerEvents(playerEventListener, this);

        // Register commands
        UmamiCommand umamiCommand = new UmamiCommand(this, configManager, umamiService);
        getCommand("umami").setExecutor(umamiCommand);
        getCommand("umami").setTabCompleter(umamiCommand);

        getLogger().info("Umami Analytics plugin has been enabled!");
        
        // Send startup event
        umamiService.sendEvent("minecraft_server_startup", null);
    }

    @Override
    public void onDisable() {
        // Send shutdown event
        if (umamiService != null) {
            umamiService.sendEvent("minecraft_server_shutdown", null);
            umamiService.shutdown();
        }

        getLogger().info("Umami Analytics plugin has been disabled!");
    }

    /**
     * Get the configuration manager
     * @return ConfigManager instance
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get the Umami service
     * @return UmamiService instance
     */
    public UmamiService getUmamiService() {
        return umamiService;
    }
}
