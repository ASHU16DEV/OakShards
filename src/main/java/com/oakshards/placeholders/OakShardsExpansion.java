package com.oakshards.placeholders;

import com.oakshards.OakShards;
import com.oakshards.afk.AFKArea;
import com.oakshards.database.DatabaseManager;
import com.oakshards.utils.NumberFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OakShardsExpansion extends PlaceholderExpansion {

    private final OakShards plugin;

    public OakShardsExpansion(OakShards plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "oakshards";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ASHU16";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null) return "";
        
        Player player = offlinePlayer.getPlayer();
        DatabaseManager.PlayerData data = player != null ? 
            plugin.getDatabaseManager().getPlayerData(player) : null;

        switch (params.toLowerCase()) {
            case "balance":
                if (data != null) {
                    return String.valueOf(data.getBalance());
                }
                return "0";

            case "formatted_balance":
                if (data != null) {
                    return NumberFormatter.format(data.getBalance());
                }
                return "0";

            case "short_balance":
                if (data != null) {
                    return NumberFormatter.formatShort(data.getBalance());
                }
                return "0";

            case "lifetime_earned":
                if (data != null) {
                    return String.valueOf(data.getLifetimeEarned());
                }
                return "0";

            case "formatted_lifetime_earned":
                if (data != null) {
                    return NumberFormatter.format(data.getLifetimeEarned());
                }
                return "0";

            case "in_afk_area":
                if (player != null) {
                    return plugin.getAFKManager().isInAFKArea(player) ? "true" : "false";
                }
                return "false";

            case "in_afk_area_yes_no":
                if (player != null) {
                    return plugin.getAFKManager().isInAFKArea(player) ? "Yes" : "No";
                }
                return "No";

            case "afk_area_name":
                if (player != null) {
                    AFKArea area = plugin.getAFKManager().getCurrentArea(player);
                    return area != null ? area.getName() : "None";
                }
                return "None";

            case "next_shard_seconds":
                if (player != null) {
                    return String.valueOf(plugin.getAFKManager().getTimeRemaining(player));
                }
                return "0";

            case "next_shard_formatted":
                if (player != null) {
                    int remaining = plugin.getAFKManager().getTimeRemaining(player);
                    return NumberFormatter.formatTimeFormatted(remaining);
                }
                return "00";

            case "shard_interval":
                if (player != null) {
                    AFKArea area = plugin.getAFKManager().getCurrentArea(player);
                    if (area != null) {
                        return String.valueOf(area.getIntervalSeconds());
                    }
                }
                return "0";

            case "shards_per_interval":
                if (player != null) {
                    AFKArea area = plugin.getAFKManager().getCurrentArea(player);
                    if (area != null) {
                        return String.valueOf(area.getShardsPerInterval());
                    }
                }
                return "0";

            case "rank":
                if (player != null) {
                    int rank = plugin.getDatabaseManager().getPlayerRank(player.getUniqueId());
                    return rank > 0 ? String.valueOf(rank) : "N/A";
                }
                return "N/A";

            default:
                return null;
        }
    }
}
