package world.hv2.umami.config;

import org.bukkit.configuration.file.FileConfiguration;
import world.hv2.umami.UmamiPlugin;

/**
 * Manages configuration for the Umami plugin.
 */
public class ConfigManager {
    
    private final UmamiPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(UmamiPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Load the configuration from config.yml
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Validate configuration
        validateConfig();
    }

    /**
     * Reload the configuration
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        validateConfig();
    }

    /**
     * Validate the configuration settings
     */
    private void validateConfig() {
        if (!isEnabled()) {
            return;
        }
        if (getApiEndpoint().equals("https://your-umami-instance.com/api/send")) {
            plugin.getLogger().warning("Umami API endpoint not configured! Please update config.yml");
        }
        
        if (getWebsiteId().equals("your-website-id")) {
            plugin.getLogger().warning("Umami website ID not configured! Please update config.yml");
        }
    }

    // API Configuration
    public boolean isEnabled() {
        return config.getBoolean("umami.enabled", false);
    }

    public String getApiEndpoint() {
        return config.getString("umami.api.endpoint", "https://your-umami-instance.com/api/send");
    }

    public String getWebsiteId() {
        return config.getString("umami.api.website_id", "your-website-id");
    }

    public String getApiKey() {
        return config.getString("umami.api.api_key", "");
    }

    public int getTimeout() {
        return config.getInt("umami.api.timeout", 5000);
    }

    public int getRetryAttempts() {
        return config.getInt("umami.api.retry_attempts", 3);
    }

    public int getRetryDelay() {
        return config.getInt("umami.api.retry_delay", 1000);
    }

    // Tracking Configuration
    public boolean isPlayerLoginTracked() {
        return config.getBoolean("umami.tracking.player_login", true);
    }

    public boolean isPlayerLogoutTracked() {
        return config.getBoolean("umami.tracking.player_logout", true);
    }

    public boolean isPlayerChatTracked() {
        return config.getBoolean("umami.tracking.player_chat", true);
    }

    public boolean isCraftingTracked() {
        return config.getBoolean("umami.tracking.crafting", true);
    }

    public boolean isPlayerKillsTracked() {
        return config.getBoolean("umami.tracking.player_kills", true);
    }

    public boolean isPlayerDeathsTracked() {
        return config.getBoolean("umami.tracking.player_deaths", true);
    }

    public boolean isItemPickupTracked() {
        return config.getBoolean("umami.tracking.item_pickup", true);
    }

    public boolean isServerPerformanceTracked() {
        return config.getBoolean("umami.tracking.server_performance", true);
    }

    public boolean isPlayerCountTracked() {
        return config.getBoolean("umami.tracking.player_count", true);
    }

    public boolean isWorldStatsTracked() {
        return config.getBoolean("umami.tracking.world_stats", false);
    }

    // Privacy Settings
    public boolean isAnonymizePlayersEnabled() {
        return config.getBoolean("umami.privacy.anonymize_players", false);
    }

    // Filter Settings
    public boolean isIgnoreOpsEnabled() {
        return config.getBoolean("umami.filters.ignore_ops", false);
    }

    public boolean isIgnoreCreativeEnabled() {
        return config.getBoolean("umami.filters.ignore_creative", false);
    }

    public int getMinSessionTime() {
        return config.getInt("umami.filters.min_session_time", 30);
    }

    // Debug Settings
    public boolean isDebugEnabled() {
        return config.getBoolean("umami.debug.enabled", false);
    }

    public boolean isLogApiCallsEnabled() {
        return config.getBoolean("umami.debug.log_api_calls", false);
    }

    public boolean isLogEventsEnabled() {
        return config.getBoolean("umami.debug.log_events", false);
    }

    public boolean isIgnoreSslCertificatesEnabled() {
        return config.getBoolean("umami.api.ignore_ssl_certificates", false);
    }
}
