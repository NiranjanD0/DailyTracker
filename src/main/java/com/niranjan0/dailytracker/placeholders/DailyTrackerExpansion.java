package com.niranjan0.dailytracker.placeholders;

import com.niranjan0.dailytracker.DailyTracker;
import com.niranjan0.dailytracker.data.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class DailyTrackerExpansion extends PlaceholderExpansion {
    
    private final DailyTracker plugin;
    
    public DailyTrackerExpansion(DailyTracker plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getIdentifier() {
        return "dat";
    }
    
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
            return null;
        }
        
        PlayerData data = plugin.getDataManager().getPlayerData(offlinePlayer.getUniqueId());
        if (data == null) {
            return "0";
        }
        
        return switch (params.toLowerCase()) {
            case "uptime_day" -> data.getFormattedDailyUptime();
            case "uptime_day_raw" -> String.valueOf(data.getTotalDailyUptime());
            case "uptime_week" -> data.getFormattedWeeklyUptime();
            case "uptime_week_raw" -> String.valueOf(data.getTotalWeeklyUptime());
            case "uptime_month" -> data.getFormattedMonthlyUptime();
            case "uptime_month_raw" -> String.valueOf(data.getTotalMonthlyUptime());
            case "downtime_month" -> {
                long downtime = plugin.getDataManager().getTotalDowntime();
                yield com.niranjan0.dailytracker.utils.Util.formatTime(downtime);
            }
            case "downtime_month_raw" -> String.valueOf(plugin.getDataManager().getTotalDowntime());
            case "is_afk" -> String.valueOf(data.isAfk());
            case "is_online" -> String.valueOf(data.isOnline());
            default -> null;
        };
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        return onRequest(player, params);
    }
}