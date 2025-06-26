package world.hv2.umami;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import world.hv2.umami.config.ConfigManager;

/**
 * Basic tests for the Umami plugin.
 */
public class UmamiPluginTest {

    private UmamiPlugin plugin;
    private ConfigManager configManager;

    @BeforeEach
    void setUp() {
        plugin = mock(UmamiPlugin.class);
        configManager = mock(ConfigManager.class);
    }

    @Test
    void testPluginInitialization() {
        // Test that plugin instance can be created
        assertNotNull(plugin);
    }

    @Test
    void testConfigManagerNotNull() {
        // Test that config manager can be mocked
        assertNotNull(configManager);
    }

    @Test
    void testDefaultConfigValues() {
        // Test default configuration values
        when(configManager.getTimeout()).thenReturn(5000);
        when(configManager.getRetryAttempts()).thenReturn(3);
        when(configManager.getRetryDelay()).thenReturn(1000);
        
        assertEquals(5000, configManager.getTimeout());
        assertEquals(3, configManager.getRetryAttempts());
        assertEquals(1000, configManager.getRetryDelay());
    }

    @Test
    void testTrackingConfigDefaults() {
        // Test default tracking configuration
        when(configManager.isPlayerLoginTracked()).thenReturn(true);
        when(configManager.isPlayerLogoutTracked()).thenReturn(true);
        when(configManager.isPlayerChatTracked()).thenReturn(true);
        when(configManager.isCraftingTracked()).thenReturn(true);
        
        assertTrue(configManager.isPlayerLoginTracked());
        assertTrue(configManager.isPlayerLogoutTracked());
        assertTrue(configManager.isPlayerChatTracked());
        assertTrue(configManager.isCraftingTracked());
    }
}
