package com.oakshards.afk;

import com.oakshards.OakShards;
import com.oakshards.utils.ColorUtils;
import com.oakshards.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AFKManager {

    private final OakShards plugin;
    private final Map<UUID, AFKPlayerData> playerAFKData = new ConcurrentHashMap<>();
    private BukkitTask actionBarTask;
    private BukkitTask shardTask;
    private long lastTickTime;

    public AFKManager(OakShards plugin) {
        this.plugin = plugin;
        this.lastTickTime = System.currentTimeMillis();
        startTasks();
    }

    public void reloadAreas() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkPlayerArea(player);
        }
    }

    private void startTasks() {
        int actionBarInterval = plugin.getConfigManager().getActionBarUpdateInterval();
        actionBarTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateActionBars, 1L, actionBarInterval);
        
        shardTask = Bukkit.getScheduler().runTaskTimer(plugin, this::processShardRewards, 1L, 20L);
    }

    private void updateActionBars() {
        if (!plugin.getConfigManager().isActionBarEnabled()) return;

        for (Map.Entry<UUID, AFKPlayerData> entry : playerAFKData.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;

            AFKPlayerData data = entry.getValue();
            if (data.getCurrentArea() == null) continue;

            int remaining = (int) Math.ceil(data.getTimeRemainingSmooth());
            String format = data.getCurrentArea().getActionBarFormat();
            
            int minutes = remaining / 60;
            int seconds = remaining % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
            
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("time_remaining", String.valueOf(remaining));
            placeholders.put("time_formatted", timeFormatted);
            placeholders.put("max_time", String.valueOf(data.getCurrentArea().getIntervalSeconds()));
            placeholders.put("current_shards", String.valueOf(plugin.getDatabaseManager().getBalance(player)));
            placeholders.put("earned_amount", String.valueOf(data.getCurrentArea().getShardsPerInterval()));
            placeholders.put("afk_area_name", data.getCurrentArea().getName());
            placeholders.put("player", player.getName());

            MessageUtils.sendActionBar(player, format, placeholders);
        }
    }

    private void processShardRewards() {
        long currentTime = System.currentTimeMillis();
        double deltaTime = (currentTime - lastTickTime) / 1000.0;
        lastTickTime = currentTime;

        for (Map.Entry<UUID, AFKPlayerData> entry : playerAFKData.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) continue;

            if (!canEarnShards(player)) continue;

            AFKPlayerData data = entry.getValue();
            if (data.getCurrentArea() == null) continue;

            data.decrementTime(deltaTime);

            if (data.getTimeRemainingSmooth() <= 0) {
                int amount = data.getCurrentArea().getShardsPerInterval();
                plugin.getDatabaseManager().addShards(player.getUniqueId(), amount);
                
                String message = plugin.getMessagesManager().getShardEarned();
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("amount", String.valueOf(amount));
                placeholders.put("plural", amount == 1 ? "" : "s");
                MessageUtils.sendMessage(player, message, placeholders);
                
                data.resetTimer();
            }
        }
    }

    private boolean canEarnShards(Player player) {
        if (plugin.getConfigManager().isRequirePlayerAlive() && player.isDead()) {
            return false;
        }
        
        if (plugin.getConfigManager().isIgnoreSpectator() && player.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }
        
        return true;
    }

    public void checkPlayerArea(Player player) {
        AFKArea currentArea = plugin.getAFKAreasManager().getAreaAtLocation(player.getLocation());
        AFKPlayerData data = playerAFKData.get(player.getUniqueId());

        if (currentArea != null) {
            if (data == null || data.getCurrentArea() == null || !data.getCurrentArea().getName().equals(currentArea.getName())) {
                enterArea(player, currentArea);
            }
        } else {
            if (data != null && data.getCurrentArea() != null) {
                leaveArea(player);
            }
        }
    }

    private void enterArea(Player player, AFKArea area) {
        AFKPlayerData data = playerAFKData.computeIfAbsent(player.getUniqueId(), k -> new AFKPlayerData());
        data.setCurrentArea(area);
        data.resetTimer();

        String title = area.getEntryTitle();
        String subtitle = area.getEntrySubtitle();
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("afk_area_name", area.getName());
        placeholders.put("interval", String.valueOf(area.getIntervalSeconds()));
        placeholders.put("amount", String.valueOf(area.getShardsPerInterval()));

        MessageUtils.sendTitle(player, 
            MessageUtils.replacePlaceholders(title, placeholders),
            MessageUtils.replacePlaceholders(subtitle, placeholders),
            plugin.getConfigManager().getTitleFadeIn(),
            plugin.getConfigManager().getTitleStay(),
            plugin.getConfigManager().getTitleFadeOut());
    }

    private void leaveArea(Player player) {
        AFKPlayerData data = playerAFKData.get(player.getUniqueId());
        if (data == null || data.getCurrentArea() == null) return;

        AFKArea area = data.getCurrentArea();
        String title = area.getLeaveTitle();
        String subtitle = area.getLeaveSubtitle();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("afk_area_name", area.getName());

        MessageUtils.sendTitle(player,
            MessageUtils.replacePlaceholders(title, placeholders),
            MessageUtils.replacePlaceholders(subtitle, placeholders),
            plugin.getConfigManager().getTitleFadeIn(),
            plugin.getConfigManager().getTitleStay(),
            plugin.getConfigManager().getTitleFadeOut());

        data.setCurrentArea(null);
    }

    public void handlePlayerJoin(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayerArea(player), 20L);
    }

    public void handlePlayerQuit(Player player) {
        playerAFKData.remove(player.getUniqueId());
    }

    public boolean isInAFKArea(Player player) {
        AFKPlayerData data = playerAFKData.get(player.getUniqueId());
        return data != null && data.getCurrentArea() != null;
    }

    public AFKArea getCurrentArea(Player player) {
        AFKPlayerData data = playerAFKData.get(player.getUniqueId());
        return data != null ? data.getCurrentArea() : null;
    }

    public int getTimeRemaining(Player player) {
        AFKPlayerData data = playerAFKData.get(player.getUniqueId());
        return data != null ? (int) Math.ceil(data.getTimeRemainingSmooth()) : 0;
    }

    public double getTimeRemainingSmooth(Player player) {
        AFKPlayerData data = playerAFKData.get(player.getUniqueId());
        return data != null ? data.getTimeRemainingSmooth() : 0;
    }

    public void shutdown() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }
        if (shardTask != null) {
            shardTask.cancel();
        }
        playerAFKData.clear();
    }

    private static class AFKPlayerData {
        private AFKArea currentArea;
        private double timeRemaining;

        public AFKArea getCurrentArea() { return currentArea; }
        public void setCurrentArea(AFKArea currentArea) { this.currentArea = currentArea; }
        
        public double getTimeRemainingSmooth() { return timeRemaining; }
        public void decrementTime(double delta) { 
            if (timeRemaining > 0) {
                timeRemaining -= delta;
                if (timeRemaining < 0) timeRemaining = 0;
            }
        }
        
        public void resetTimer() {
            if (currentArea != null) {
                timeRemaining = currentArea.getIntervalSeconds();
            }
        }
    }
}
