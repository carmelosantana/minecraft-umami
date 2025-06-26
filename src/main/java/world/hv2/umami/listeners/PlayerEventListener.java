package world.hv2.umami.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import world.hv2.umami.UmamiPlugin;
import world.hv2.umami.config.ConfigManager;
import world.hv2.umami.services.UmamiService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens to player events and sends analytics data to Umami.
 */
public class PlayerEventListener implements Listener {
    
    private final UmamiPlugin plugin;
    private final UmamiService umamiService;
    private final ConfigManager configManager;
    private final Map<Player, Long> playerJoinTimes;

    public PlayerEventListener(UmamiPlugin plugin, UmamiService umamiService) {
        this.plugin = plugin;
        this.umamiService = umamiService;
        this.configManager = plugin.getConfigManager();
        this.playerJoinTimes = new ConcurrentHashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!configManager.isPlayerLoginTracked()) {
            return;
        }

        Player player = event.getPlayer();
        
        // Skip if filtering is enabled
        if (shouldIgnorePlayer(player)) {
            return;
        }

        // Record join time for session tracking
        playerJoinTimes.put(player, System.currentTimeMillis());

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("first_join", !player.hasPlayedBefore());
        eventData.put("op", player.isOp());
        
        umamiService.sendPlayerEvent("minecraft_player_login", player, eventData);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!configManager.isPlayerLogoutTracked()) {
            return;
        }

        Player player = event.getPlayer();
        
        // Skip if filtering is enabled
        if (shouldIgnorePlayer(player)) {
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        
        // Calculate session time
        Long joinTime = playerJoinTimes.remove(player);
        if (joinTime != null) {
            long sessionTime = (System.currentTimeMillis() - joinTime) / 1000; // Convert to seconds
            eventData.put("session_time", sessionTime);
            
            // Skip if session too short
            if (sessionTime < configManager.getMinSessionTime()) {
                return;
            }
        }
        
        eventData.put("op", player.isOp());
        
        umamiService.sendPlayerEvent("minecraft_player_logout", player, eventData);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!configManager.isPlayerChatTracked() || event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        
        // Skip if filtering is enabled
        if (shouldIgnorePlayer(player)) {
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("message_length", event.getMessage().length());
        eventData.put("recipients", event.getRecipients().size());
        
        umamiService.sendPlayerEvent("minecraft_player_chat", player, eventData);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCraftItem(CraftItemEvent event) {
        if (!configManager.isCraftingTracked() || event.isCancelled()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        
        // Skip if filtering is enabled
        if (shouldIgnorePlayer(player)) {
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("item", event.getRecipe().getResult().getType().name().toLowerCase());
        eventData.put("amount", event.getRecipe().getResult().getAmount());
        
        umamiService.sendPlayerEvent("minecraft_player_craft", player, eventData);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!configManager.isPlayerDeathsTracked()) {
            return;
        }

        Player player = event.getEntity();
        
        // Skip if filtering is enabled
        if (shouldIgnorePlayer(player)) {
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        
        if (event.getDeathMessage() != null) {
            eventData.put("death_message", event.getDeathMessage());
        }
        
        // Determine cause of death
        if (player.getKiller() != null) {
            eventData.put("killer", player.getKiller().getName());
            eventData.put("death_type", "pvp");
        } else {
            eventData.put("death_type", "environment");
        }
        
        eventData.put("keep_inventory", !event.getKeepInventory());
        eventData.put("exp_dropped", event.getDroppedExp());
        
        umamiService.sendPlayerEvent("minecraft_player_death", player, eventData);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!configManager.isItemPickupTracked() || event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        
        // Skip if filtering is enabled
        if (shouldIgnorePlayer(player)) {
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("item", event.getItem().getItemStack().getType().name().toLowerCase());
        eventData.put("amount", event.getItem().getItemStack().getAmount());
        
        umamiService.sendPlayerEvent("minecraft_player_pickup", player, eventData);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!configManager.isPlayerKillsTracked()) {
            return;
        }

        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        
        // Skip if filtering is enabled
        if (shouldIgnorePlayer(killer)) {
            return;
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("victim_type", event.getEntity().getType().name().toLowerCase());
        eventData.put("exp_dropped", event.getDroppedExp());
        
        // Check if it's a player kill
        if (event.getEntity() instanceof Player) {
            eventData.put("kill_type", "pvp");
            eventData.put("victim_name", event.getEntity().getName());
        } else {
            eventData.put("kill_type", "mob");
        }
        
        umamiService.sendPlayerEvent("minecraft_player_kill", killer, eventData);
    }

    /**
     * Check if a player should be ignored based on filter settings
     */
    private boolean shouldIgnorePlayer(Player player) {
        // Ignore ops if configured
        if (configManager.isIgnoreOpsEnabled() && player.isOp()) {
            return true;
        }
        
        // Ignore creative mode players if configured
        if (configManager.isIgnoreCreativeEnabled() && player.getGameMode() == GameMode.CREATIVE) {
            return true;
        }
        
        return false;
    }
}
