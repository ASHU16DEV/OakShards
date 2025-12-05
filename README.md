# OakShards

An AFK-based shard currency plugin for Minecraft Spigot/Paper servers (1.16.5-1.21.10). Players earn shards by standing in designated AFK areas, which can then be spent in a fully configurable GUI shop.

## Features

- **Multiple AFK Areas** - Create unlimited AFK zones with individual timers and rewards
- **Live Action Bar Timer** - Smooth countdown display in mm:ss format, updates every second
- **GUI Shop System** - Fully customizable shop with in-game editor
- **AFK Area Editor GUI** - Create and manage AFK areas entirely in-game
- **Dynamic Shop Lore** - Shows "Click to buy" or "Insufficient Shards" based on player balance
- **Item Serialization** - Support for items with enchantments, book data, and custom NBT
- **Two-Way Sync** - Changes made in-game automatically save to config files
- **SQLite Database** - Efficient player data storage
- **PlaceholderAPI Support** - Full integration for scoreboards, holograms, and more

## Commands

### Player Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/shards` | View your shard balance | `oakshards.player.balance` |
| `/shards <player>` | View another player's balance | `oakshards.player.balance.others` |
| `/shards top [page]` | View top shard holders | `oakshards.player.top` |
| `/shardshop` | Open the shard shop | `oakshards.player.shop` |
| `/afkinfo` | View your AFK status | `oakshards.player.afkinfo` |

### Admin Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/oakshards reload` | Reload all configurations | `oakshards.admin.reload` |
| `/oakshards give <player> <amount>` | Give shards to a player | `oakshards.admin.give` |
| `/oakshards take <player> <amount>` | Take shards from a player | `oakshards.admin.take` |
| `/oakshards set <player> <amount>` | Set a player's shard balance | `oakshards.admin.set` |
| `/oakshards setpos1` | Set AFK area position 1 | `oakshards.admin.afkarea` |
| `/oakshards setpos2` | Set AFK area position 2 | `oakshards.admin.afkarea` |
| `/oakshards createarea <name>` | Create an AFK area | `oakshards.admin.afkarea` |
| `/oakshards removearea <name>` | Remove an AFK area | `oakshards.admin.afkarea` |
| `/oakshards listareas` | List all AFK areas | `oakshards.admin.afkarea` |
| `/oakshards shopedit` | Open the shop editor GUI | `oakshards.admin.shop.edit` |
| `/oakshards areaedit` | Open the AFK area editor GUI | `oakshards.admin.afkarea` |

## PlaceholderAPI Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%oakshards_balance%` | Current shard balance |
| `%oakshards_formatted_balance%` | Formatted balance with commas |
| `%oakshards_short_balance%` | Short balance (1k, 1M, etc.) |
| `%oakshards_lifetime_earned%` | Total shards earned all-time |
| `%oakshards_in_afk_area%` | true/false if in AFK area |
| `%oakshards_afk_area_name%` | Current AFK area name |
| `%oakshards_next_shard_seconds%` | Seconds until next shard |
| `%oakshards_next_shard_formatted%` | Formatted time (mm:ss) |
| `%oakshards_shard_interval%` | Area's shard interval |
| `%oakshards_shards_per_interval%` | Shards earned per interval |
| `%oakshards_rank%` | Player's rank on leaderboard |

## Configuration

### config.yml
```yaml
general:
  give-only-in-afk-areas: true    # Only earn shards in designated areas
  require-player-alive: true       # Must be alive to earn shards
  ignore-spectator: true           # Spectators don't earn shards

actionbar:
  enabled: true
  update-interval-ticks: 20        # Action bar update frequency
  format: "&aNext shard in: &f%time_formatted%"

titles:
  use-per-area-messages: true      # Use area-specific titles
  fade-in: 10
  stay: 40
  fade-out: 10
```

### afkareas.yml
```yaml
afk-areas:
  spawn_afk:
    enabled: true
    world: world
    pos1: "100,60,100"
    pos2: "120,70,120"
    interval-seconds: 60           # Time between shard rewards
    shards-per-interval: 1         # Shards earned per interval
    entry-title: "&a&lAFK Zone"
    entry-subtitle: "&7Earn shards by standing here"
    leave-title: "&c&lLeaving AFK Zone"
    leave-subtitle: "&7You stopped earning shards"
    actionbar-format: "&aNext shard in: &f%time_formatted%"
```

### shop.yml
```yaml
shop:
  title: "&8&lShard Shop"
  rows: 3
  items:
    example_item:
      slot: 13
      material: DIAMOND
      amount: 1
      display-name: "&b&lDiamond"
      lore:
        - "&7A shiny diamond!"
      cost: 100
      buy-commands:
        - "give %player% diamond 1"
```

## In-Game Editors

### Shop Editor (`/oakshards shopedit`)
- Add/remove shop items
- Set material, name, lore, cost
- Add buy commands with `%player%` placeholder
- Hold an item and click "Give Item from Hand" to save enchanted items

### AFK Area Editor (`/oakshards areaedit`)
- View all AFK areas with status indicators
- Create new areas with position selection
- Edit all area properties in-game
- Toggle areas enabled/disabled
- Delete areas with confirmation

## Installation

1. Download the latest `OakShards-1.0.0.jar`
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure the plugin in `plugins/OakShards/`

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `oakshards.player.balance` | View own balance | true |
| `oakshards.player.balance.others` | View others' balance | op |
| `oakshards.player.top` | View leaderboard | true |
| `oakshards.player.shop` | Open shop | true |
| `oakshards.player.afkinfo` | View AFK status | true |
| `oakshards.admin.reload` | Reload configs | op |
| `oakshards.admin.give` | Give shards | op |
| `oakshards.admin.take` | Take shards | op |
| `oakshards.admin.set` | Set balance | op |
| `oakshards.admin.afkarea` | Manage AFK areas | op |
| `oakshards.admin.shop.edit` | Edit shop | op |

## Requirements

- Spigot/Paper 1.16.5 or higher
- Java 8+
- PlaceholderAPI (optional)

## Author

ASHU16

## Support

For issues or feature requests, please open an issue on the repository.
