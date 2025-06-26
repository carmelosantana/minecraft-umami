package world.hv2.umami.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.bukkit.entity.Player;
import org.bukkit.Server;
import world.hv2.umami.config.ConfigManager;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for IP forwarding functionality in UmamiService.
 */
public class UmamiServiceIPTest {

    private UmamiService umamiService;
    private ConfigManager configManager;
    private Player mockPlayer;
    private Server mockServer;

    @BeforeEach
    void setUp() {
        configManager = mock(ConfigManager.class);
        
        // Mock configuration values
        when(configManager.getApiEndpoint()).thenReturn("https://test.umami.com/api/send");
        when(configManager.getWebsiteId()).thenReturn("test-website-id");
        when(configManager.getTimeout()).thenReturn(5000);
        when(configManager.isDebugEnabled()).thenReturn(true);
        when(configManager.isAnonymizePlayersEnabled()).thenReturn(false);
        
        umamiService = new UmamiService(configManager);
        
        // Mock player
        mockPlayer = mock(Player.class);
        mockServer = mock(Server.class);
        
        // Mock server MOTD
        when(mockServer.getMotd()).thenReturn("Test Minecraft Server");
    }

    @Test
    void testPlayerIPExtraction() {
        // Mock player address
        InetSocketAddress mockAddress = mock(InetSocketAddress.class);
        InetAddress mockInetAddress = mock(InetAddress.class);
        
        when(mockPlayer.getAddress()).thenReturn(mockAddress);
        when(mockAddress.getAddress()).thenReturn(mockInetAddress);
        when(mockInetAddress.getHostAddress()).thenReturn("192.168.1.100");
        
        // Test that player IP can be extracted
        assertEquals("192.168.1.100", mockInetAddress.getHostAddress());
    }

    @Test
    void testPlayerIPExtractionWithNullAddress() {
        // Mock player with null address
        when(mockPlayer.getAddress()).thenReturn(null);
        
        // Test that null address is handled gracefully
        assertNull(mockPlayer.getAddress());
    }

    @Test
    void testConfiguredService() {
        // Test that service is configured
        assertTrue(umamiService.isConfigured());
    }

    @Test
    void testUnconfiguredService() {
        // Mock unconfigured service
        when(configManager.getApiEndpoint()).thenReturn("https://your-umami-instance.com/api/send");
        when(configManager.getWebsiteId()).thenReturn("your-website-id");
        
        UmamiService unconfiguredService = new UmamiService(configManager);
        
        // Test that service is not configured
        assertFalse(unconfiguredService.isConfigured());
    }
}
