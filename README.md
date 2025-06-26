# 📊 Umami Minecraft Plugin

A powerful analytics plugin that tracks **player activity** through the Umami API, providing server administrators with valuable insights into player behavior and server usage patterns.

## 🎯 Features

- **Comprehensive Player Tracking**: Login/logout, chat, crafting, combat, block interactions
- **Privacy-Focused**: Configurable anonymization of player data and locations
- **Asynchronous Processing**: Event queue system prevents server lag
- **Robust API Client**: Built-in retry logic and error handling
- **Admin Commands**: Easy management and monitoring tools
- **Docker Support**: Ready for containerized deployments

## 📋 Tracked Events

### Player Activity

| Event Type | Description | Data Points |
|------------|-------------|-------------|
| 🔐 **Login/Logout** | Player join/leave events | Player UUID, world, session duration |
| 💬 **Chat Messages** | Public messages | Message count/content, location, timestamp |
| 🎒 **Item Pickup** | Items collected by players | Item type, quantity, location |
| 🔨 **Crafting** | Items crafted by players | Recipe type, quantity, location |
| ⚔️ **Combat Events** | Player kills and deaths | Cause of death, weapon used, PvP vs PvE |
| 🧱 **Block Interactions** | Block placement and breaking | Block type, location, tool used |

## 🛠 Installation

### Requirements

- **Java**: 21 or higher
- **Server**: Paper 1.21.6+, Spigot 1.21+, or Bukkit 1.21+
- **Maven**: For building from source
- **Umami Instance**: Self-hosted or cloud Umami analytics

### Quick Install

1. Download the latest JAR from releases
2. Place in your server's `plugins/` directory
3. Configure your Umami settings in `plugins/Umami/config.yml`
4. Restart your server
5. Verify data is flowing to your Umami dashboard

### Build from Source

```bash
# Clone the repository
git clone https://github.com/carmelosantana/minecraft-umami
cd minecraft-umami

# Build the plugin
make build

# Install to test server
make install
```

## ⚙️ Configuration

Edit `plugins/Umami/config.yml`:

```yaml
# Umami Tracker Plugin Configuration
umami:
  # Umami API Configuration
  api:
    endpoint: "https://your-umami-instance.com/api/send"
    website_id: "your-website-id"
    api_key: "your-api-key"
    timeout: 5000 # Request timeout in milliseconds
    retry_attempts: 3
    retry_delay: 1000 # Delay between retries in milliseconds

  # Event Tracking Configuration
  tracking:
    # Player Activity Events
    player_login: true
    player_logout: true
    player_chat: true
    item_pickup: true
    crafting: true
    combat_events: true
    block_interactions: true
    
  # Privacy Settings
  privacy:
    anonymize_players: false # Hash player UUIDs
    anonymize_chat: true # Don't send chat content, only message length
    anonymize_locations: true # Round coordinates to nearest 100 blocks
    
  # Filtering
  filters:
    ignore_ops: false # Ignore operator activity
    ignore_creative: false # Ignore creative mode players
    min_session_time: 30 # Minimum session time to track (seconds)
    
  # Debug Settings
  debug:
    enabled: false # Enable debug logging
    log_api_calls: false # Log all API calls
    log_events: false # Log all tracked events

# Enable debug logging (shows detailed operation information)
debug: false
```

## 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/umami help` | Show command help | `umami.use` |
| `/umami version` | Display plugin info | `umami.use` |
| `/umami online` | Show current users online | `umami.use` |
| `/umami reload` | Reload configuration | `umami.admin` |
| `/umami status` | Show plugin status and queue info | `umami.admin` |
| `/umami test` | Send a test event to Umami | `umami.admin` |

### Examples

```bash
/umami status        # Check plugin status
/umami test          # Send test event
/umami online        # List online players
/umami reload        # Reload config
```

## 📝 Permissions

- `umami.use` - Basic command usage (default: true)
- `umami.admin` - Admin commands (default: op)

## 🔧 Development

### Quick Development Cycle

```bash
make dev             # Build, install, restart server
make debug           # Interactive debug menu
make test-commands   # Show available test commands
```

### Server Management

```bash
make setup           # Set up development environment
make start           # Start Minecraft server
make stop            # Stop server
make restart         # Restart server
make logs            # View server logs
make status          # Check server status
```

### Docker Testing

```bash
make docker-build    # Build Docker container
make docker-test     # Test in container
```

## 📊 Umami Dashboard Integration

### Custom Events

The plugin sends custom events to Umami with the following structure:

```json
{
  "type": "event",
  "payload": {
    "website": "your-website-id",
    "name": "minecraft_player_login",
    "data": {
      "player": "player_12345",
      "world": "world_nether",
      "gamemode": "survival",
      "location": "100,64,200"
    }
  }
}
```

### Event Categories

- `minecraft_player_*` - Player activity events
- `minecraft_server_*` - Server performance events (future)
- `minecraft_world_*` - World-specific events (future)
- `minecraft_plugin_*` - Plugin-specific metrics

## 🔒 Privacy & Security

### Data Anonymization

- **Player Anonymization**: Hash UUIDs instead of using names
- **Location Anonymization**: Round coordinates to nearest 100 blocks
- **Chat Anonymization**: Send message length only, not content

### Security Best Practices

- Store API keys securely
- Use HTTPS endpoints only
- Regular security updates
- Monitor API access logs

## 🐳 Docker Support

Compatible with the [Legendary Minecraft Geyser Docker container](https://github.com/TheRemote/Legendary-Java-Minecraft-Geyser-Floodgate):

```yaml
services:
  minecraft:
    image: 05jchambers/legendary-minecraft-geyser-floodgate:latest
    ports:
      - "25565:25565"
      - "19132:19132"
    volumes:
      - ./umami-1.0.0.jar:/minecraft/plugins/umami-1.0.0.jar
```

## 🛡️ Error Handling

- **Graceful API Failures**: Plugin continues working even if Umami is down
- **Automatic Retries**: Configurable retry attempts with exponential backoff
- **Queue Management**: Prevents memory issues with queue size limits
- **Configuration Validation**: Validates settings on startup

## 📈 Performance

- **Asynchronous Processing**: All API calls run on separate threads
- **Event Queuing**: Batched processing prevents server lag
- **Memory Efficient**: Automatic cleanup and queue size limits
- **Minimal Overhead**: Event-driven architecture with caching

## 🔧 Technical Details

### Architecture

- **Event-driven**: Minimal performance impact using Bukkit event system
- **Asynchronous**: All network operations run on separate threads
- **Modular**: Clean separation between tracking, queuing, and API communication

### Dependencies

- **Paper API**: 1.21.6+ for server integration
- **OkHttp**: HTTP client for Umami API communication
- **Gson**: JSON processing for event data

### Compatibility

- **Paper**: 1.21.6+ (recommended)
- **Spigot**: 1.21+
- **Bukkit**: 1.21+
- **Java**: 21+
- **Geyser/Floodgate**: Compatible

## 🐛 Troubleshooting

### Common Issues

**Plugin not tracking events:**
- Check if Umami configuration is correct
- Verify API endpoint is accessible
- Check server logs for error messages
- Use `/umami test` to verify connectivity

**High memory usage:**
- Reduce event queue size in configuration
- Check for network connectivity issues causing queue backup
- Monitor queue statistics with `/umami status`

**Performance issues:**
- Disable unnecessary event types
- Increase API timeout settings
- Check network latency to Umami instance

### Debug Mode

Enable debug logging in config.yml:

```yaml
debug: true
umami:
  debug:
    enabled: true
    log_api_calls: true
    log_events: true
```

## 📄 License

Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)

## 👥 Credits

- **Author**: Carmelo Santana
- **Website**: https://hv2.world
- **Live Server**: play.hv2.world
- **Docker Container**: [Legendary Minecraft Geyser](https://github.com/TheRemote/Legendary-Java-Minecraft-Geyser-Floodgate)

## 📞 Support

For support and questions:
- Check the [documentation](https://hv2.world)
- Review server logs for error messages
- Test connectivity with `/umami test`
- Visit our live server: `play.hv2.world`

## 🚀 Future Enhancements

- Server performance metrics
- World-specific analytics
- Advanced player behavior analysis
- Integration with other analytics platforms
- Real-time dashboard widgets
