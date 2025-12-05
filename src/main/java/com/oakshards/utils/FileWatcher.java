package com.oakshards.utils;

import com.oakshards.OakShards;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileWatcher {

    private final OakShards plugin;
    private WatchService watchService;
    private Thread watchThread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Map<String, Long> lastModified = new HashMap<>();

    public FileWatcher(OakShards plugin) {
        this.plugin = plugin;
    }

    public void start() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path dataFolder = plugin.getDataFolder().toPath();
            
            dataFolder.register(watchService, 
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE);
            
            running.set(true);
            
            watchThread = new Thread(() -> {
                while (running.get()) {
                    try {
                        WatchKey key = watchService.poll(
                            plugin.getConfigManager().getFileWatcherInterval(),
                            java.util.concurrent.TimeUnit.SECONDS
                        );
                        
                        if (key == null) continue;
                        
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;
                            
                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                            Path filename = pathEvent.context();
                            String name = filename.toString();
                            
                            if (name.endsWith(".yml")) {
                                File file = new File(plugin.getDataFolder(), name);
                                long currentModified = file.lastModified();
                                Long previous = lastModified.get(name);
                                
                                if (previous == null || currentModified > previous) {
                                    lastModified.put(name, currentModified);
                                    
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        handleFileChange(name);
                                    });
                                }
                            }
                        }
                        
                        key.reset();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "OakShards-FileWatcher");
            
            watchThread.setDaemon(true);
            watchThread.start();
            
            plugin.getLogger().info("File watcher started!");
        } catch (IOException e) {
            plugin.getLogger().warning("Could not start file watcher: " + e.getMessage());
        }
    }

    private void handleFileChange(String filename) {
        switch (filename) {
            case "config.yml":
                plugin.getConfigManager().reload();
                plugin.getLogger().info("config.yml reloaded automatically!");
                break;
            case "messages.yml":
                plugin.getMessagesManager().reload();
                plugin.getLogger().info("messages.yml reloaded automatically!");
                break;
            case "shop.yml":
                plugin.getShopManager().reload();
                plugin.getLogger().info("shop.yml reloaded automatically!");
                break;
            case "afkareas.yml":
                plugin.getAFKAreasManager().reload();
                plugin.getAFKManager().reloadAreas();
                plugin.getLogger().info("afkareas.yml reloaded automatically!");
                break;
        }
    }

    public void stop() {
        running.set(false);
        
        if (watchThread != null) {
            watchThread.interrupt();
        }
        
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                plugin.getLogger().warning("Error closing file watcher: " + e.getMessage());
            }
        }
    }

    public void updateLastModified(String filename) {
        File file = new File(plugin.getDataFolder(), filename);
        lastModified.put(filename, file.lastModified());
    }
}
