package com.oakshards.config;

import com.oakshards.OakShards;
import com.oakshards.afk.AFKArea;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AFKAreasManager {

    private final OakShards plugin;
    private FileConfiguration areasConfig;
    private File areasFile;
    private final Map<String, AFKArea> afkAreas = new HashMap<>();

    public AFKAreasManager(OakShards plugin) {
        this.plugin = plugin;
        loadAreasConfig();
    }

    private void loadAreasConfig() {
        areasFile = new File(plugin.getDataFolder(), "afkareas.yml");
        if (!areasFile.exists()) {
            plugin.saveResource("afkareas.yml", false);
        }
        areasConfig = YamlConfiguration.loadConfiguration(areasFile);
        loadAreas();
    }

    public void reload() {
        areasConfig = YamlConfiguration.loadConfiguration(areasFile);
        loadAreas();
    }

    private void loadAreas() {
        afkAreas.clear();
        ConfigurationSection areasSection = areasConfig.getConfigurationSection("afk-areas");
        if (areasSection == null) return;

        for (String key : areasSection.getKeys(false)) {
            ConfigurationSection areaSection = areasSection.getConfigurationSection(key);
            if (areaSection == null) continue;

            boolean enabled = areaSection.getBoolean("enabled", true);
            if (!enabled) continue;

            String worldName = areaSection.getString("world", "world");
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("World '" + worldName + "' not found for AFK area: " + key);
                continue;
            }

            String pos1Str = areaSection.getString("pos1", "0,0,0");
            String pos2Str = areaSection.getString("pos2", "0,0,0");

            Location pos1 = parseLocation(world, pos1Str);
            Location pos2 = parseLocation(world, pos2Str);

            if (pos1 == null || pos2 == null) {
                plugin.getLogger().warning("Invalid positions for AFK area: " + key);
                continue;
            }

            AFKArea area = new AFKArea(key, world, pos1, pos2);
            area.setEnabled(enabled);
            
            int interval = areaSection.getInt("interval-seconds", plugin.getConfigManager().getDefaultShardInterval());
            int shards = areaSection.getInt("shards-per-interval", plugin.getConfigManager().getDefaultShardsPerInterval());
            
            if (interval <= 0) interval = plugin.getConfigManager().getDefaultShardInterval();
            if (shards <= 0) shards = plugin.getConfigManager().getDefaultShardsPerInterval();
            
            area.setIntervalSeconds(interval);
            area.setShardsPerInterval(shards);
            area.setEntryTitle(areaSection.getString("entry-title", plugin.getConfigManager().getGlobalEntryTitle()));
            area.setEntrySubtitle(areaSection.getString("entry-subtitle", plugin.getConfigManager().getGlobalEntrySubtitle()));
            area.setLeaveTitle(areaSection.getString("leave-title", plugin.getConfigManager().getGlobalLeaveTitle()));
            area.setLeaveSubtitle(areaSection.getString("leave-subtitle", plugin.getConfigManager().getGlobalLeaveSubtitle()));
            area.setActionBarFormat(areaSection.getString("actionbar-format", plugin.getConfigManager().getActionBarFormat()));

            afkAreas.put(key, area);
        }

        plugin.getLogger().info("Loaded " + afkAreas.size() + " AFK areas.");
    }

    private Location parseLocation(World world, String locationStr) {
        try {
            String[] parts = locationStr.split(",");
            double x = Double.parseDouble(parts[0].trim());
            double y = Double.parseDouble(parts[1].trim());
            double z = Double.parseDouble(parts[2].trim());
            return new Location(world, x, y, z);
        } catch (Exception e) {
            return null;
        }
    }

    private String locationToString(Location loc) {
        return loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public void save() {
        ConfigurationSection areasSection = areasConfig.createSection("afk-areas");

        for (Map.Entry<String, AFKArea> entry : afkAreas.entrySet()) {
            AFKArea area = entry.getValue();
            ConfigurationSection areaSection = areasSection.createSection(entry.getKey());

            areaSection.set("enabled", area.isEnabled());
            areaSection.set("world", area.getWorld().getName());
            areaSection.set("pos1", locationToString(area.getPos1()));
            areaSection.set("pos2", locationToString(area.getPos2()));
            areaSection.set("interval-seconds", area.getIntervalSeconds());
            areaSection.set("shards-per-interval", area.getShardsPerInterval());
            areaSection.set("entry-title", area.getEntryTitle());
            areaSection.set("entry-subtitle", area.getEntrySubtitle());
            areaSection.set("leave-title", area.getLeaveTitle());
            areaSection.set("leave-subtitle", area.getLeaveSubtitle());
            areaSection.set("actionbar-format", area.getActionBarFormat());
        }

        try {
            areasConfig.save(areasFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save afkareas.yml: " + e.getMessage());
        }
    }

    public Map<String, AFKArea> getAFKAreas() {
        return Collections.unmodifiableMap(afkAreas);
    }

    public AFKArea getAFKArea(String name) {
        return afkAreas.get(name);
    }

    public boolean areaExists(String name) {
        return afkAreas.containsKey(name);
    }

    public void createArea(String name, Location pos1, Location pos2) {
        AFKArea area = new AFKArea(name, pos1.getWorld(), pos1, pos2);
        area.setEnabled(true);
        area.setIntervalSeconds(plugin.getConfigManager().getDefaultShardInterval());
        area.setShardsPerInterval(plugin.getConfigManager().getDefaultShardsPerInterval());
        area.setEntryTitle(plugin.getConfigManager().getGlobalEntryTitle());
        area.setEntrySubtitle(plugin.getConfigManager().getGlobalEntrySubtitle());
        area.setLeaveTitle(plugin.getConfigManager().getGlobalLeaveTitle());
        area.setLeaveSubtitle(plugin.getConfigManager().getGlobalLeaveSubtitle());
        area.setActionBarFormat(plugin.getConfigManager().getActionBarFormat());

        afkAreas.put(name, area);
        save();
    }

    public void createAreaWithSettings(String name, Location pos1, Location pos2, int intervalSeconds, int shardsPerInterval) {
        AFKArea area = new AFKArea(name, pos1.getWorld(), pos1, pos2);
        area.setEnabled(true);
        area.setIntervalSeconds(intervalSeconds);
        area.setShardsPerInterval(shardsPerInterval);
        area.setEntryTitle(plugin.getConfigManager().getGlobalEntryTitle());
        area.setEntrySubtitle(plugin.getConfigManager().getGlobalEntrySubtitle());
        area.setLeaveTitle(plugin.getConfigManager().getGlobalLeaveTitle());
        area.setLeaveSubtitle(plugin.getConfigManager().getGlobalLeaveSubtitle());
        area.setActionBarFormat(plugin.getConfigManager().getActionBarFormat());

        afkAreas.put(name, area);
        save();
    }

    public void updateArea(AFKArea area) {
        afkAreas.put(area.getName(), area);
        save();
    }

    public void removeArea(String name) {
        afkAreas.remove(name);
        save();
    }

    public AFKArea getAreaAtLocation(Location location) {
        for (AFKArea area : afkAreas.values()) {
            if (area.isEnabled() && area.contains(location)) {
                return area;
            }
        }
        return null;
    }
}
