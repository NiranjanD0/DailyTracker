package com.niranjan0.dailytracker.data;

import com.niranjan0.dailytracker.DailyTracker;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DataManager {
    
    private final DailyTracker plugin;
    private final Map<UUID, PlayerData> playerDataMap;
    private Connection connection;
    private LocalDateTime downtimeStart;
    
    public DataManager(DailyTracker plugin) {
        this.plugin = plugin;
        this.playerDataMap = new ConcurrentHashMap<>();
    }
    
    public boolean initialize() {
        if (!setupDatabase()) {
            return false;
        }
        
        loadDowntimeData();
        return true;
    }
    
    private boolean setupDatabase() {
        try {
            File databaseFile = new File(plugin.getDataFolder(), "dailytracker.db");
            String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
            
            connection = DriverManager.getConnection(url);
            
            // Create tables
            createTables();
            
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to setup database", e);
            return false;
        }
    }
    
    private void createTables() throws SQLException {
        String createPlayerDataTable = """
            CREATE TABLE IF NOT EXISTS player_data (
                player_id TEXT PRIMARY KEY,
                player_name TEXT NOT NULL,
                daily_uptime INTEGER DEFAULT 0,
                weekly_uptime INTEGER DEFAULT 0,
                monthly_uptime INTEGER DEFAULT 0,
                last_daily_reset DATE DEFAULT CURRENT_DATE,
                last_week_reset INTEGER DEFAULT 1,
                last_week_year INTEGER DEFAULT 2024,
                last_month_reset INTEGER DEFAULT 1,
                last_month_year INTEGER DEFAULT 2024
            )
        """;
        
        String createDowntimeTable = """
            CREATE TABLE IF NOT EXISTS downtime_data (
                id INTEGER PRIMARY KEY,
                total_downtime INTEGER DEFAULT 0,
                last_shutdown TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                start_date DATE
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPlayerDataTable);
            stmt.execute(createDowntimeTable);
        }
        
        // Initialize downtime data if not exists
        initializeDowntimeData();
    }
    
    private void initializeDowntimeData() throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM downtime_data";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                String startDateStr = plugin.getConfigManager().getConfig().getString("monthly-downtime-start-date", "2024-01-01");
                String insertQuery = "INSERT INTO downtime_data (total_downtime, start_date) VALUES (0, ?)";
                
                try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
                    pstmt.setString(1, startDateStr);
                    pstmt.execute();
                }
            }
        }
    }
    
    private void loadDowntimeData() {
        try {
            String query = "SELECT last_shutdown FROM downtime_data ORDER BY id DESC LIMIT 1";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                if (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("last_shutdown");
                    if (timestamp != null) {
                        downtimeStart = timestamp.toLocalDateTime();
                        updateDowntime();
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load downtime data", e);
        }
    }
    
    public PlayerData getPlayerData(UUID playerId) {
        PlayerData data = playerDataMap.get(playerId);
        if (data == null) {
            data = loadPlayerData(playerId);
            if (data != null) {
                playerDataMap.put(playerId, data);
            }
        }
        return data;
    }
    
    public PlayerData getOrCreatePlayerData(UUID playerId, String playerName) {
        PlayerData data = getPlayerData(playerId);
        if (data == null) {
            data = new PlayerData(playerId, playerName);
            playerDataMap.put(playerId, data);
            savePlayerData(data);
        } else {
            data.setPlayerName(playerName);
        }
        return data;
    }
    
    private PlayerData loadPlayerData(UUID playerId) {
        String query = """
            SELECT * FROM player_data WHERE player_id = ?
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, playerId.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    PlayerData data = new PlayerData(playerId, rs.getString("player_name"));
                    data.setDailyUptime(rs.getLong("daily_uptime"));
                    data.setWeeklyUptime(rs.getLong("weekly_uptime"));
                    data.setMonthlyUptime(rs.getLong("monthly_uptime"));
                    
                    String dateStr = rs.getString("last_daily_reset");
                    if (dateStr != null) {
                        data.setLastDailyReset(LocalDate.parse(dateStr));
                    }
                    
                    data.setLastWeekReset(rs.getInt("last_week_reset"));
                    data.setLastWeekYear(rs.getInt("last_week_year"));
                    data.setLastMonthReset(rs.getInt("last_month_reset"));
                    data.setLastMonthYear(rs.getInt("last_month_year"));
                    
                    return data;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + playerId, e);
        }
        
        return null;
    }
    
    public void savePlayerData(PlayerData data) {
        if (data == null) return;
        
        String query = """
            INSERT OR REPLACE INTO player_data (
                player_id, player_name, daily_uptime, weekly_uptime, monthly_uptime,
                last_daily_reset, last_week_reset, last_week_year, last_month_reset, last_month_year
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, data.getPlayerId().toString());
            pstmt.setString(2, data.getPlayerName());
            pstmt.setLong(3, data.getDailyUptime());
            pstmt.setLong(4, data.getWeeklyUptime());
            pstmt.setLong(5, data.getMonthlyUptime());
            pstmt.setString(6, data.getLastDailyReset().toString());
            pstmt.setInt(7, data.getLastWeekReset());
            pstmt.setInt(8, data.getLastWeekYear());
            pstmt.setInt(9, data.getLastMonthReset());
            pstmt.setInt(10, data.getLastMonthYear());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + data.getPlayerName(), e);
        }
    }
    
    public void saveAllData() {
        for (PlayerData data : playerDataMap.values()) {
            if (data.isOnline()) {
                data.endSession();
                data.startSession();
            }
            savePlayerData(data);
        }
        updateDowntime();
    }
    
    public List<PlayerData> getTopPlayers(String period, int limit) {
        String orderColumn = switch (period.toLowerCase()) {
            case "weekly" -> "weekly_uptime";
            case "monthly" -> "monthly_uptime";
            default -> "daily_uptime";
        };
        
        String query = "SELECT * FROM player_data ORDER BY " + orderColumn + " DESC LIMIT ?";
        List<PlayerData> topPlayers = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UUID playerId = UUID.fromString(rs.getString("player_id"));
                    PlayerData data = getPlayerData(playerId);
                    if (data != null) {
                        topPlayers.add(data);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get top players", e);
        }
        
        return topPlayers;
    }
    
    private void updateDowntime() {
        if (downtimeStart == null) {
            downtimeStart = LocalDateTime.now();
            return;
        }
        
        long downtimeSeconds = java.time.Duration.between(downtimeStart, LocalDateTime.now()).getSeconds();
        
        String query = "UPDATE downtime_data SET total_downtime = total_downtime + ?, last_shutdown = CURRENT_TIMESTAMP";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setLong(1, downtimeSeconds);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update downtime", e);
        }
        
        downtimeStart = LocalDateTime.now();
    }
    
    public long getTotalDowntime() {
        String query = "SELECT total_downtime FROM downtime_data ORDER BY id DESC LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                long totalDowntime = rs.getLong("total_downtime");
                if (downtimeStart != null) {
                    long currentDowntime = java.time.Duration.between(downtimeStart, LocalDateTime.now()).getSeconds();
                    return totalDowntime + currentDowntime;
                }
                return totalDowntime;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get total downtime", e);
        }
        return 0;
    }
    
    public void resetDowntime() {
        String query = "UPDATE downtime_data SET total_downtime = 0, last_shutdown = CURRENT_TIMESTAMP";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
            downtimeStart = LocalDateTime.now();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reset downtime", e);
        }
    }
    
    public void removePlayerData(UUID playerId) {
        playerDataMap.remove(playerId);
    }
    
    public void close() {
        if (connection != null) {
            try {
                updateDowntime();
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close database connection", e);
            }
        }
    }
}