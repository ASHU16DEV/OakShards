package com.oakshards.config;

import com.oakshards.OakShards;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {

    private final OakShards plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(OakShards plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public boolean isGiveOnlyInAFKAreas() {
        return config.getBoolean("general.give-only-in-afk-areas", true);
    }

    public boolean isRequirePlayerAlive() {
        return config.getBoolean("general.require-player-alive", true);
    }

    public boolean isIgnoreSpectator() {
        return config.getBoolean("general.ignore-spectator", true);
    }

    public int getSaveInterval() {
        return config.getInt("general.save-interval-seconds", 120);
    }

    public int getDefaultShardInterval() {
        return 60;
    }

    public int getDefaultShardsPerInterval() {
        return 1;
    }

    public boolean isActionBarEnabled() {
        return config.getBoolean("actionbar.enabled", true);
    }

    public int getActionBarUpdateInterval() {
        return config.getInt("actionbar.update-interval-ticks", 20);
    }

    public String getActionBarFormat() {
        return config.getString("actionbar.format", "&aNext shard in: &f%time_formatted%");
    }

    public boolean usePerAreaMessages() {
        return config.getBoolean("titles.use-per-area-messages", true);
    }

    public int getTitleFadeIn() {
        return config.getInt("titles.fade-in", 10);
    }

    public int getTitleStay() {
        return config.getInt("titles.stay", 40);
    }

    public int getTitleFadeOut() {
        return config.getInt("titles.fade-out", 10);
    }

    public String getGlobalEntryTitle() {
        return config.getString("global-entry-title", "&a&lAFK Zone");
    }

    public String getGlobalEntrySubtitle() {
        return config.getString("global-entry-subtitle", "&7Earn shards by standing here");
    }

    public String getGlobalLeaveTitle() {
        return config.getString("global-leave-title", "&c&lLeaving AFK Zone");
    }

    public String getGlobalLeaveSubtitle() {
        return config.getString("global-leave-subtitle", "&7You will no longer earn shards");
    }

    public String getStorageMethod() {
        return config.getString("storage.method", "sqlite");
    }

    public String getStorageFile() {
        return config.getString("storage.file", "data.db");
    }

    public boolean isFileWatcherEnabled() {
        return config.getBoolean("file-watcher.enabled", true);
    }

    public int getFileWatcherInterval() {
        return config.getInt("file-watcher.check-interval-seconds", 2);
    }

    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }
}
