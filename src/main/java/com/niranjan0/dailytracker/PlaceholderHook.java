package com.niranjan0.dailytracker;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion {

    private final DailyTracker plugin;
    private final PlayerDataManager pdm;
    private final DowntimeManager dm;

    public PlaceholderHook(DailyTracker plugin, PlayerDataManager pdm, DowntimeManager dm) {
        this.plugin = plugin;
        this.pdm = pdm;
        this.dm = dm;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "dailytracker";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Niranjan0";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        var data = pdm.getPlayerData(player);

        switch (identifier.toLowerCase()) {
            case "uptime_day":
                return Util.formatTime(data.getDailyUptime());
            case "uptime_day_raw":
                return String.valueOf(data.getDailyUptime());
            case "uptime_week":
                return Util.formatTime(data.getWeeklyUptime());
            case "uptime_week_raw":
                return String.valueOf(data.getWeeklyUptime());
            case "uptime_month":
                return Util.formatTime(data.getMonthlyUptime());
            case "uptime_month_raw":
                return String.valueOf(data.getMonthlyUptime());
            case "downtime_month":
                return Util.formatTime(dm.getDowntimeSeconds());
            case "downtime_month_raw":
                return String.valueOf(dm.getDowntimeSeconds());
        }

        return null;
    }
}