package com.oakshards;

import com.oakshards.commands.*;
import com.oakshards.config.*;
import com.oakshards.database.DatabaseManager;
import com.oakshards.afk.AFKManager;
import com.oakshards.gui.GUIListener;
import com.oakshards.listeners.PlayerListener;
import com.oakshards.placeholders.OakShardsExpansion;
import com.oakshards.utils.FileWatcher;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class OakShards extends JavaPlugin {

    private static OakShards instance;
    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private ShopManager shopManager;
    private AFKAreasManager afkAreasManager;
    private DatabaseManager databaseManager;
    private AFKManager afkManager;
    private FileWatcher fileWatcher;

    @Override
    public void onEnable() {
        instance = this;
        
        loadConfigs();
        
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        
        afkManager = new AFKManager(this);
        
        registerCommands();
        registerListeners();
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new OakShardsExpansion(this).register();
            getLogger().info("PlaceholderAPI found! Placeholders registered.");
        }
        
        if (configManager.isFileWatcherEnabled()) {
            fileWatcher = new FileWatcher(this);
            fileWatcher.start();
        }
        
        startAutoSave();
        
        getLogger().info("OakShards has been enabled!");
    }

    @Override
    public void onDisable() {
        if (fileWatcher != null) {
            fileWatcher.stop();
        }
        
        if (afkManager != null) {
            afkManager.shutdown();
        }
        
        if (databaseManager != null) {
            databaseManager.saveAllPlayers();
            databaseManager.close();
        }
        
        getLogger().info("OakShards has been disabled!");
    }

    private void loadConfigs() {
        configManager = new ConfigManager(this);
        messagesManager = new MessagesManager(this);
        shopManager = new ShopManager(this);
        afkAreasManager = new AFKAreasManager(this);
    }

    public void reloadAllConfigs() {
        configManager.reload();
        messagesManager.reload();
        shopManager.reload();
        afkAreasManager.reload();
        
        if (afkManager != null) {
            afkManager.reloadAreas();
        }
    }

    private void registerCommands() {
        ShardsCommand shardsCommand = new ShardsCommand(this);
        getCommand("shards").setExecutor(shardsCommand);
        getCommand("shards").setTabCompleter(shardsCommand);
        
        ShardShopCommand shopCommand = new ShardShopCommand(this);
        getCommand("shardshop").setExecutor(shopCommand);
        
        AFKInfoCommand afkInfoCommand = new AFKInfoCommand(this);
        getCommand("afkinfo").setExecutor(afkInfoCommand);
        
        OakShardsCommand oakShardsCommand = new OakShardsCommand(this);
        getCommand("oakshards").setExecutor(oakShardsCommand);
        getCommand("oakshards").setTabCompleter(oakShardsCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
    }

    private void startAutoSave() {
        int saveInterval = configManager.getSaveInterval() * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            databaseManager.saveAllPlayers();
        }, saveInterval, saveInterval);
    }

    public static OakShards getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public AFKAreasManager getAFKAreasManager() {
        return afkAreasManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public AFKManager getAFKManager() {
        return afkManager;
    }
}
