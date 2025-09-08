package com.niranjan0.dailytracker.config;

import com.niranjan0.dailytracker.DailyTracker;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class ConfigManager {
    
    private final DailyTracker plugin;
    private FileConfiguration config;
    private FileConfiguration guiConfig;
    
    public ConfigManager(DailyTracker plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        // Load main config
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Load GUI config
        loadGuiConfig();
        
        // Validate configurations
        validateConfig();
    }
    
    private void loadGuiConfig() {
        File guiFile = new File(plugin.getDataFolder(), "gui.yml");
        if (!guiFile.exists()) {
            plugin.saveResource("gui.yml", false);
        }
        
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        
        // Load defaults
        InputStream defConfigStream = plugin.getResource("gui.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            guiConfig.setDefaults(defConfig);
        }
    }
    
    private void validateConfig() {
        boolean modified = false;
        
        // Check monthly reset day
        int resetDay = config.getInt("monthly-player-reset-day", 1);
        if (resetDay < 1 || resetDay > 28) {
            plugin.getLogger().warning("Invalid monthly-player-reset-day (" + resetDay + "). Setting to 1.");
            config.set("monthly-player-reset-day", 1);
            modified = true;
        }
        
        // Check AFK timeout
        int afkTimeout = config.getInt("afk.timeout", 300);
        if (afkTimeout < 60) {
            plugin.getLogger().warning("AFK timeout too low (" + afkTimeout + "s). Setting to 300s.");
            config.set("afk.timeout", 300);
            modified = true;
        }
        
        if (modified) {
            try {
                config.save(new File(plugin.getDataFolder(), "config.yml"));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config.yml after validation", e);
            }
        }
    }
    
    public String getMessage(String key) {
        String message = config.getString("messages." + key, "Message not found: " + key);
        return message.replace("&", "§");
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }
}