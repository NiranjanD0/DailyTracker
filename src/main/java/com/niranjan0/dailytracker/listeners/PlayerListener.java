package com.niranjan0.dailytracker.listeners;

import com.niranjan0.dailytracker.DailyTracker;
import com.niranjan0.dailytracker.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
    
    private final DailyTracker plugin;
    private final Map<UUID, LocalDateTime> lastActivity = new HashMap<>();
    
    public PlayerListener(DailyTracker plugin) {
        this.plugin = plugin;
        startAfkCheckTask();
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        PlayerData data = plugin.getDataManager().getOrCreatePlayerData(playerId, player.getName());
        data.startSession();
        
        lastActivity.put(playerId, LocalDateTime.now());
        
        if (plugin.getConfigManager().getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info(player.getName() + " joined - session started");
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        PlayerData data = plugin.getDataManager().getPlayerData(playerId);
        if (data != null) {
            data.endSession();
            plugin.getDataManager().savePlayerData(data);
        }
        
        lastActivity.remove(playerId);
        
        if (plugin.getConfigManager().getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info(player.getName() + " left - session ended");
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            updatePlayerActivity(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        updatePlayerActivity(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        updatePlayerActivity(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        updatePlayerActivity(event.getPlayer());
    }
    
    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            updatePlayerActivity(player);
        }
    }
    
    private void updatePlayerActivity(Player player) {
        UUID playerId = player.getUniqueId();
        lastActivity.put(playerId, LocalDateTime.now());
        
        PlayerData data = plugin.getDataManager().getPlayerData(playerId);
        if (data != null) {
            data.updateActivity();
        }
    }
    
    private void startAfkCheckTask() {
        boolean trackAfk = plugin.getConfigManager().getConfig().getBoolean("track-afk", true);
        if (!trackAfk) return;
        
        int checkInterval = plugin.getConfigManager().getConfig().getInt("afk.check-interval", 30);
        int afkTimeout = plugin.getConfigManager().getConfig().getInt("afk.timeout", 300);
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            LocalDateTime now = LocalDateTime.now();
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID playerId = player.getUniqueId();
                LocalDateTime lastAct = lastActivity.get(playerId);
                
                if (lastAct != null) {
                    long secondsSinceActivity = java.time.Duration.between(lastAct, now).getSeconds();
                    boolean shouldBeAfk = secondsSinceActivity >= afkTimeout;
                    
                    PlayerData data = plugin.getDataManager().getPlayerData(playerId);
                    if (data != null && data.isAfk() != shouldBeAfk) {
                        data.setAfk(shouldBeAfk);
                        
                        if (plugin.getConfigManager().getConfig().getBoolean("debug", false)) {
                            plugin.getLogger().info(player.getName() + " AFK status changed to: " + shouldBeAfk);
                        }
                    }
                }
            }
        }, checkInterval * 20L, checkInterval * 20L);
    }
}