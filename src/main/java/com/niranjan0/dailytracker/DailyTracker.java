package com.niranjan0.dailytracker;

import com.niranjan0.dailytracker.commands.DatCommand;
import com.niranjan0.dailytracker.config.ConfigManager;
import com.niranjan0.dailytracker.data.DataManager;
import com.niranjan0.dailytracker.listeners.PlayerListener;
import com.niranjan0.dailytracker.placeholders.DailyTrackerExpansion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class DailyTracker extends JavaPlugin {
    
    private static DailyTracker instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private DailyTrackerExpansion placeholderExpansion;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        // Initialize data manager
        dataManager = new DataManager(this);
        if (!dataManager.initialize()) {
            getLogger().severe("Failed to initialize data manager! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Register commands
        getCommand("dat").setExecutor(new DatCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Register PlaceholderAPI expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new DailyTrackerExpansion(this);
            if (placeholderExpansion.register()) {
                getLogger().info("Successfully registered PlaceholderAPI expansion!");
            }
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
        }
        
        // Initialize bStats if enabled
        if (configManager.getConfig().getBoolean("bstats", true)) {
            new Metrics(this, 20000); // Replace with actual bStats plugin ID
        }
        
        // Start auto-save task
        startAutoSaveTask();
        
        getLogger().info("DailyTracker has been enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAllData();
            dataManager.close();
        }
        
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
        
        getLogger().info("DailyTracker has been disabled!");
    }
    
    private void startAutoSaveTask() {
        int interval = configManager.getConfig().getInt("auto-save-interval", 5) * 60 * 20; // Convert minutes to ticks
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                dataManager.saveAllData();
                if (configManager.getConfig().getBoolean("debug", false)) {
                    getLogger().info("Auto-saved all player data");
                }
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Error during auto-save", e);
            }
        }, interval, interval);
    }
    
    public void reload() {
        configManager.loadConfigs();
        getLogger().info("Configuration reloaded!");
    }
    
    // Getters
    public static DailyTracker getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
}