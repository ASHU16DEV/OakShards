package com.oakshards.database;

import com.oakshards.OakShards;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {

    private final OakShards plugin;
    private Connection connection;
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();

    public DatabaseManager(OakShards plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String dbFile = plugin.getConfigManager().getStorageFile();
            String url = "jdbc:sqlite:" + new File(dataFolder, dbFile).getAbsolutePath();
            
            connection = DriverManager.getConnection(url);
            
            createTables();
            
            plugin.getLogger().info("SQLite database connected successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to SQLite database: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS player_data (" +
                "uuid TEXT PRIMARY KEY," +
                "username TEXT," +
                "balance BIGINT DEFAULT 0," +
                "lifetime_earned BIGINT DEFAULT 0," +
                "last_seen BIGINT" +
                ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void loadPlayer(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String sql = "SELECT * FROM player_data WHERE uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, player.getUniqueId().toString());
                    ResultSet rs = stmt.executeQuery();
                    
                    PlayerData data;
                    if (rs.next()) {
                        data = new PlayerData(player.getUniqueId());
                        data.setBalance(rs.getLong("balance"));
                        data.setLifetimeEarned(rs.getLong("lifetime_earned"));
                    } else {
                        data = new PlayerData(player.getUniqueId());
                        insertPlayer(player, data);
                    }
                    
                    data.setUsername(player.getName());
                    playerDataCache.put(player.getUniqueId(), data);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not load player data: " + e.getMessage());
            }
        });
    }

    private void insertPlayer(Player player, PlayerData data) throws SQLException {
        String sql = "INSERT INTO player_data (uuid, username, balance, lifetime_earned, last_seen) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, player.getUniqueId().toString());
            stmt.setString(2, player.getName());
            stmt.setLong(3, data.getBalance());
            stmt.setLong(4, data.getLifetimeEarned());
            stmt.setLong(5, System.currentTimeMillis());
            stmt.executeUpdate();
        }
    }

    public void savePlayer(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String sql = "UPDATE player_data SET username = ?, balance = ?, lifetime_earned = ?, last_seen = ? WHERE uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, data.getUsername());
                    stmt.setLong(2, data.getBalance());
                    stmt.setLong(3, data.getLifetimeEarned());
                    stmt.setLong(4, System.currentTimeMillis());
                    stmt.setString(5, uuid.toString());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not save player data: " + e.getMessage());
            }
        });
    }

    public void saveAllPlayers() {
        for (UUID uuid : playerDataCache.keySet()) {
            savePlayer(uuid);
        }
    }

    public void unloadPlayer(UUID uuid) {
        savePlayer(uuid);
        playerDataCache.remove(uuid);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataCache.get(player.getUniqueId());
    }

    public long getBalance(Player player) {
        PlayerData data = getPlayerData(player);
        return data != null ? data.getBalance() : 0;
    }

    public void setBalance(UUID uuid, long amount) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            data.setBalance(amount);
            savePlayer(uuid);
        }
    }

    public void addShards(UUID uuid, long amount) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            data.setBalance(data.getBalance() + amount);
            data.setLifetimeEarned(data.getLifetimeEarned() + amount);
            savePlayer(uuid);
        }
    }

    public boolean removeShards(UUID uuid, long amount) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null && data.getBalance() >= amount) {
            data.setBalance(data.getBalance() - amount);
            savePlayer(uuid);
            return true;
        }
        return false;
    }

    public boolean hasEnoughShards(UUID uuid, long amount) {
        PlayerData data = playerDataCache.get(uuid);
        return data != null && data.getBalance() >= amount;
    }

    public List<Map.Entry<String, Long>> getTopPlayers(int limit) {
        List<Map.Entry<String, Long>> topList = new ArrayList<>();
        
        try {
            String sql = "SELECT username, balance FROM player_data ORDER BY balance DESC LIMIT ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    String username = rs.getString("username");
                    long balance = rs.getLong("balance");
                    topList.add(new AbstractMap.SimpleEntry<>(username, balance));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get top players: " + e.getMessage());
        }
        
        return topList;
    }

    public int getPlayerRank(UUID uuid) {
        try {
            PlayerData data = playerDataCache.get(uuid);
            if (data == null) return -1;
            
            String sql = "SELECT COUNT(*) + 1 as rank FROM player_data WHERE balance > ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, data.getBalance());
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt("rank");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get player rank: " + e.getMessage());
        }
        return -1;
    }

    public PlayerData getOfflinePlayerData(String username) {
        try {
            String sql = "SELECT * FROM player_data WHERE LOWER(username) = LOWER(?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    PlayerData data = new PlayerData(UUID.fromString(rs.getString("uuid")));
                    data.setUsername(rs.getString("username"));
                    data.setBalance(rs.getLong("balance"));
                    data.setLifetimeEarned(rs.getLong("lifetime_earned"));
                    return data;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not get offline player data: " + e.getMessage());
        }
        return null;
    }

    public void setOfflineBalance(String username, long amount) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String sql = "UPDATE player_data SET balance = ? WHERE LOWER(username) = LOWER(?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setLong(1, amount);
                    stmt.setString(2, username);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not update offline player balance: " + e.getMessage());
            }
        });
    }

    public void addOfflineShards(String username, long amount) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String sql = "UPDATE player_data SET balance = balance + ?, lifetime_earned = lifetime_earned + ? WHERE LOWER(username) = LOWER(?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setLong(1, amount);
                    stmt.setLong(2, amount);
                    stmt.setString(3, username);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not add offline shards: " + e.getMessage());
            }
        });
    }

    public boolean removeOfflineShards(String username, long amount) {
        PlayerData data = getOfflinePlayerData(username);
        if (data != null && data.getBalance() >= amount) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    String sql = "UPDATE player_data SET balance = balance - ? WHERE LOWER(username) = LOWER(?) AND balance >= ?";
                    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                        stmt.setLong(1, amount);
                        stmt.setString(2, username);
                        stmt.setLong(3, amount);
                        stmt.executeUpdate();
                    }
                } catch (SQLException e) {
                    plugin.getLogger().severe("Could not remove offline shards: " + e.getMessage());
                }
            });
            return true;
        }
        return false;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not close database connection: " + e.getMessage());
        }
    }

    public static class PlayerData {
        private final UUID uuid;
        private String username;
        private long balance;
        private long lifetimeEarned;

        public PlayerData(UUID uuid) {
            this.uuid = uuid;
            this.balance = 0;
            this.lifetimeEarned = 0;
        }

        public UUID getUuid() { return uuid; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public long getBalance() { return balance; }
        public void setBalance(long balance) { this.balance = balance; }
        public long getLifetimeEarned() { return lifetimeEarned; }
        public void setLifetimeEarned(long lifetimeEarned) { this.lifetimeEarned = lifetimeEarned; }
    }
}
