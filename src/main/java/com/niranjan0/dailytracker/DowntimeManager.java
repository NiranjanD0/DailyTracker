package com.niranjan0.dailytracker;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.bukkit.configuration.file.YamlConfiguration;

public class DowntimeManager {

    private final DailyTracker plugin;
    private final File file;
    private final FileConfiguration config;
    private long downtimeSeconds;
    private String monthlyStartDate;

    public DowntimeManager(DailyTracker plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "downtime.yml");
        if (!file.exists()) plugin.saveResource("downtime.yml", false);
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public void load() {
        downtimeSeconds = config.getLong("downtime-seconds", 0);
        monthlyStartDate = config.getString("monthly-start-date", "2025-01-01");
    }

    public void save() {
        config.set("downtime-seconds", downtimeSeconds);
        config.set("monthly-start-date", monthlyStartDate);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getDowntimeSeconds() {
        return downtimeSeconds;
    }

    public void addDowntime(long seconds) {
        downtimeSeconds += seconds;
        save();
    }

    public void resetDowntime() {
        downtimeSeconds = 0;
        save();
    }

    public LocalDate getMonthlyStartDate() {
        return LocalDate.parse(monthlyStartDate, DateTimeFormatter.ISO_DATE);
    }

    public void setMonthlyStartDate(LocalDate date) {
        this.monthlyStartDate = date.format(DateTimeFormatter.ISO_DATE);
        save();
    }
}