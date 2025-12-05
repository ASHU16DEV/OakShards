# OakShards - Minecraft Spigot/Paper Plugin

## Overview
OakShards is an AFK-based shard currency plugin for Minecraft Spigot/Paper servers (1.16.5-1.21.10). Players earn shards by standing in designated AFK areas, which can then be spent in a fully configurable GUI shop.

## Author
ASHU16

## Features
- Multiple AFK areas with individual timers and rewards
- Smooth live action bar countdown timer (mm:ss format, updates every second)
- Clickable-only GUI system (no drag/drop)
- In-game shop editor with full item management
- In-game AFK Area Editor GUI for easy area management
- Two-way sync between file and in-game edits
- Dynamic shop lore (Click to buy / Insufficient Shards)
- Support for items with enchantments/book data in shop
- SQLite database for player data
- PlaceholderAPI integration
- Hot-reload for all config files

## Recent Changes (December 2025)
- Added AFK Area Editor GUI with AFKAreaListGUI, AFKAreaCreatorGUI, AFKAreaEditorGUI
- Added /oakshards areaedit command for in-game area management
- Updated placeholder format from {placeholder} to %placeholder%
- Changed timer to 1-second intervals (20 ticks) with mm:ss format
- Enhanced Shop Editor with item from inventory support (enchantments, book data)
- Added dynamic lore system showing Click to buy / Insufficient Shards based on player balance
- Added sound effects for purchase success/failure
- Improved Shop Editor with detailed lore display and clear functionality

## Project Structure
```
OakShards/
├── pom.xml                          # Maven build configuration
├── src/main/
│   ├── java/com/oakshards/
│   │   ├── OakShards.java           # Main plugin class
│   │   ├── afk/                     # AFK system
│   │   │   ├── AFKArea.java
│   │   │   └── AFKManager.java
│   │   ├── commands/                # All commands
│   │   │   ├── AFKInfoCommand.java
│   │   │   ├── OakShardsCommand.java
│   │   │   ├── ShardShopCommand.java
│   │   │   └── ShardsCommand.java
│   │   ├── config/                  # Config managers
│   │   │   ├── AFKAreasManager.java
│   │   │   ├── ConfigManager.java
│   │   │   ├── MessagesManager.java
│   │   │   └── ShopManager.java
│   │   ├── database/                # SQLite database
│   │   │   └── DatabaseManager.java
│   │   ├── gui/                     # GUI system
│   │   │   ├── BaseGUI.java
│   │   │   ├── ConfirmGUI.java
│   │   │   ├── GUIListener.java
│   │   │   ├── ShopEditorGUI.java
│   │   │   ├── ShopGUI.java
│   │   │   └── afkeditor/           # AFK Area Editor GUIs
│   │   │       ├── AFKAreaListGUI.java
│   │   │       ├── AFKAreaCreatorGUI.java
│   │   │       └── AFKAreaEditorGUI.java
│   │   ├── listeners/               # Event listeners
│   │   │   ├── ChatListener.java    # Chat input handler
│   │   │   └── PlayerListener.java
│   │   ├── placeholders/            # PlaceholderAPI
│   │   │   └── OakShardsExpansion.java
│   │   └── utils/                   # Utilities
│   │       ├── ColorUtils.java
│   │       ├── FileWatcher.java
│   │       ├── MessageUtils.java
│   │       └── NumberFormatter.java
│   └── resources/
│       ├── plugin.yml               # Plugin metadata
│       ├── config.yml               # Main config
│       ├── afkareas.yml             # AFK areas config
│       ├── messages.yml             # Messages config
│       └── shop.yml                 # Shop config
```

## Commands
### Player Commands
- `/shards` - View shard balance
- `/shards top [page]` - View leaderboard
- `/shardshop` - Open shard shop
- `/afkinfo` - View AFK status

### Admin Commands
- `/oakshards reload` - Reload all configs
- `/oakshards give <player> <amount>` - Give shards
- `/oakshards take <player> <amount>` - Take shards
- `/oakshards set <player> <amount>` - Set balance
- `/oakshards setpos1` - Set AFK area position 1
- `/oakshards setpos2` - Set AFK area position 2
- `/oakshards createarea <name>` - Create AFK area
- `/oakshards removearea <name>` - Remove AFK area
- `/oakshards listareas` - List all AFK areas
- `/oakshards shopedit` - Open shop editor GUI
- `/oakshards areaedit` - Open AFK area editor GUI

## PlaceholderAPI Placeholders
- `%oakshards_balance%` - Current balance
- `%oakshards_formatted_balance%` - Formatted balance
- `%oakshards_short_balance%` - Short balance (1k, 1M)
- `%oakshards_lifetime_earned%` - Lifetime earned
- `%oakshards_in_afk_area%` - true/false
- `%oakshards_afk_area_name%` - Current area name
- `%oakshards_next_shard_seconds%` - Seconds to next shard
- `%oakshards_next_shard_formatted%` - Formatted time
- `%oakshards_shard_interval%` - Area interval
- `%oakshards_shards_per_interval%` - Shards per interval
- `%oakshards_rank%` - Player rank

## Build Instructions
Run `mvn clean package -DskipTests -U` to build the plugin. The JAR file will be in `target/OakShards-1.0.0.jar`.

**Current Build Status:** Complete  
**JAR Size:** 612KB (optimized - only Linux x86_64 SQLite native included)

## Dependencies
- Spigot/Paper API 1.16.5+
- SQLite JDBC (shaded, optimized for Linux x86_64 servers)
- PlaceholderAPI (optional, soft dependency)

## Technical Notes
- Action bar timer updates every 20 ticks (1 second) with mm:ss format
- GUI system is clickable-only with UI_BUTTON_CLICK sounds
- Unlimited purchases allowed (no one-time restriction)
- Inventory full check before purchases
- Two-way file sync for shop/area editors
- Per-area interval and shard settings in afkareas.yml
- Placeholder format: %placeholder% (also supports legacy {placeholder})
- Shop items can save full ItemStack data including enchantments
- Dynamic shop lore shows affordable/insufficient status per player
