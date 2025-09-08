package com.niranjan0.dailytracker;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerDataManager {

    private final DailyTracker plugin;
    private final File dataFile;
    private final FileConfiguration dataConfig;
    private final Map<UUID, PlayerData> playerDataMap;

    public PlayerDataManager(DailyTracker plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "storage.yml");
        if (!dataFile.exists()) plugin.saveResource("storage.yml", false);
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        this.playerDataMap = new HashMap<>();
        loadAll();
    }

    public void loadAll() {
        for (String uuidStr : dataConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            playerDataMap.put(uuid, new PlayerData(dataConfig.getConfigurationSection(uuidStr)));
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            entry.getValue().save(dataConfig.createSection(entry.getKey().toString()));
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerData());
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.getOrDefault(uuid, new PlayerData());
    }
}