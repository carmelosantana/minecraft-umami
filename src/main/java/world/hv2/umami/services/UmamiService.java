package world.hv2.umami.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import world.hv2.umami.config.ConfigManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Service for sending analytics data to Umami API.
 */
public class UmamiService {
    
    private final ConfigManager configManager;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Logger logger;

    public UmamiService(ConfigManager configManager) {
        this.configManager = configManager;
        this.gson = new Gson();
        this.logger = Logger.getLogger("UmamiService");
        
        // Configure HTTP client
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(configManager.getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(configManager.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(configManager.getTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * Send an event to Umami
     * 
     * @param eventName Name of the event
     * @param eventData Additional data for the event
     */
    public void sendEvent(String eventName, Map<String, Object> eventData) {
        if (!isConfigured()) {
            if (configManager.isDebugEnabled()) {
                logger.warning("Umami not configured, skipping event: " + eventName);
            }
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                sendEventSync(eventName, eventData);
            } catch (Exception e) {
                logger.severe("Failed to send event to Umami: " + e.getMessage());
                if (configManager.isDebugEnabled()) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Send event synchronously with retry logic
     */
    private void sendEventSync(String eventName, Map<String, Object> eventData) throws IOException {
        JsonObject payload = createPayload(eventName, eventData);
        
        if (configManager.isLogEventsEnabled()) {
            logger.info("Sending event: " + eventName + " with data: " + payload.toString());
        }

        RequestBody body = RequestBody.create(
            payload.toString(),
            MediaType.get("application/json; charset=utf-8")
        );

        Request.Builder requestBuilder = new Request.Builder()
                .url(configManager.getApiEndpoint())
                .post(body)
                // Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:139.0) Gecko/20100101 Firefox/139.0
                .addHeader("User-Agent", "Minecraft-Umami-Plugin/1.0.1")
                .addHeader("Content-Type", "application/json");

        // Add API key if configured
        String apiKey = configManager.getApiKey();
        if (!apiKey.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + apiKey);
        }

        Request request = requestBuilder.build();

        // Retry logic
        int attempts = 0;
        int maxAttempts = configManager.getRetryAttempts();
        
        while (attempts < maxAttempts) {
            try {
                if (configManager.isLogApiCallsEnabled()) {
                    logger.info("Making API call to: " + configManager.getApiEndpoint());
                }

                Response response = httpClient.newCall(request).execute();
                
                if (response.isSuccessful()) {
                    if (configManager.isDebugEnabled()) {
                        logger.info("Successfully sent event: " + eventName);
                    }
                    response.close();
                    return;
                } else {
                    logger.warning("Umami API returned error code: " + response.code() + " - " + response.message());
                    response.close();
                }
            } catch (IOException e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    throw e;
                }
                
                logger.warning("Failed to send event (attempt " + attempts + "/" + maxAttempts + "): " + e.getMessage());
                
                try {
                    Thread.sleep(configManager.getRetryDelay());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry delay", ie);
                }
            }
        }
    }

    /**
     * Create the JSON payload for Umami API
     */
    private JsonObject createPayload(String eventName, Map<String, Object> eventData) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "event");
        
        JsonObject payload = new JsonObject();
        payload.addProperty("hostname", "minecraft-server");
        payload.addProperty("language", "en-US");
        // payload.addProperty("referrer", "");
        // payload.addProperty("screen", "1920x1080");
        payload.addProperty("title", "Minecraft Server");
        payload.addProperty("url", "/game");
        payload.addProperty("website", configManager.getWebsiteId());
        payload.addProperty("name", eventName);
        
        // Add custom event data
        if (eventData != null && !eventData.isEmpty()) {
            JsonObject data = new JsonObject();
            for (Map.Entry<String, Object> entry : eventData.entrySet()) {
                if (entry.getValue() instanceof String) {
                    data.addProperty(entry.getKey(), (String) entry.getValue());
                } else if (entry.getValue() instanceof Number) {
                    data.addProperty(entry.getKey(), (Number) entry.getValue());
                } else if (entry.getValue() instanceof Boolean) {
                    data.addProperty(entry.getKey(), (Boolean) entry.getValue());
                } else {
                    data.addProperty(entry.getKey(), entry.getValue().toString());
                }
            }
            payload.add("data", data);
        }
        
        root.add("payload", payload);
        return root;
    }

    /**
     * Send a player event with common player data
     */
    public void sendPlayerEvent(String eventName, Player player, Map<String, Object> additionalData) {
        Map<String, Object> eventData = new HashMap<>();
        
        // Add player data
        if (configManager.isAnonymizePlayersEnabled()) {
            eventData.put("player_id", player.getUniqueId().toString().hashCode());
        } else {
            eventData.put("player_id", player.getUniqueId().toString());
            eventData.put("player_name", player.getName());
        }
        
        eventData.put("world", player.getWorld().getName());
        eventData.put("gamemode", player.getGameMode().name().toLowerCase());
        eventData.put("player_count", Bukkit.getOnlinePlayers().size());
        
        // Add additional data if provided
        if (additionalData != null) {
            eventData.putAll(additionalData);
        }
        
        sendEvent(eventName, eventData);
    }

    /**
     * Send a test event to verify configuration
     */
    public CompletableFuture<Boolean> sendTestEvent() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> testData = new HashMap<>();
                testData.put("test", true);
                testData.put("timestamp", System.currentTimeMillis());
                
                sendEventSync("minecraft_plugin_test", testData);
                return true;
            } catch (Exception e) {
                logger.severe("Test event failed: " + e.getMessage());
                return false;
            }
        });
    }

    /**
     * Check if Umami is properly configured
     */
    public boolean isConfigured() {
        return !configManager.getApiEndpoint().equals("https://your-umami-instance.com/api/send") &&
               !configManager.getWebsiteId().equals("your-website-id");
    }

    /**
     * Shutdown the service
     */
    public void shutdown() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }
}
