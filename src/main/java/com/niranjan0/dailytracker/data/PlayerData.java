package com.niranjan0.dailytracker.data;

import com.niranjan0.dailytracker.utils.Util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.UUID;

public class PlayerData {
    
    private final UUID playerId;
    private String playerName;
    
    // Session tracking
    private LocalDateTime sessionStart;
    private LocalDateTime lastActivity;
    private boolean isAfk;
    
    // Daily tracking
    private LocalDate lastDailyReset;
    private long dailyUptime; // seconds
    
    // Weekly tracking
    private int lastWeekReset;
    private int lastWeekYear;
    private long weeklyUptime; // seconds
    
    // Monthly tracking
    private int lastMonthReset;
    private int lastMonthYear;
    private long monthlyUptime; // seconds
    
    public PlayerData(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.sessionStart = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.isAfk = false;
        
        // Initialize reset dates
        LocalDate today = LocalDate.now();
        this.lastDailyReset = today;
        
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        this.lastWeekReset = today.get(weekFields.weekOfWeekBasedYear());
        this.lastWeekYear = today.getYear();
        
        this.lastMonthReset = today.getMonthValue();
        this.lastMonthYear = today.getYear();
        
        this.dailyUptime = 0;
        this.weeklyUptime = 0;
        this.monthlyUptime = 0;
    }
    
    public void startSession() {
        this.sessionStart = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.isAfk = false;
        checkResets();
    }
    
    public void endSession() {
        if (sessionStart != null) {
            updateUptime();
        }
        this.sessionStart = null;
    }
    
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
        this.isAfk = false;
    }
    
    public void setAfk(boolean afk) {
        if (this.isAfk != afk && sessionStart != null) {
            updateUptime();
            this.sessionStart = LocalDateTime.now();
        }
        this.isAfk = afk;
    }
    
    private void updateUptime() {
        if (sessionStart == null) return;
        
        long sessionDuration = java.time.Duration.between(sessionStart, LocalDateTime.now()).getSeconds();
        
        checkResets();
        
        dailyUptime += sessionDuration;
        weeklyUptime += sessionDuration;
        monthlyUptime += sessionDuration;
        
        sessionStart = LocalDateTime.now();
    }
    
    private void checkResets() {
        LocalDate today = LocalDate.now();
        
        // Check daily reset
        if (!lastDailyReset.equals(today)) {
            dailyUptime = 0;
            lastDailyReset = today;
        }
        
        // Check weekly reset
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeek = today.get(weekFields.weekOfWeekBasedYear());
        int currentYear = today.getYear();
        
        if (lastWeekReset != currentWeek || lastWeekYear != currentYear) {
            weeklyUptime = 0;
            lastWeekReset = currentWeek;
            lastWeekYear = currentYear;
        }
        
        // Check monthly reset
        int currentMonth = today.getMonthValue();
        int currentMonthYear = today.getYear();
        
        if (lastMonthReset != currentMonth || lastMonthYear != currentMonthYear) {
            monthlyUptime = 0;
            lastMonthReset = currentMonth;
            lastMonthYear = currentMonthYear;
        }
    }
    
    public long getCurrentSessionTime() {
        if (sessionStart == null) return 0;
        return java.time.Duration.between(sessionStart, LocalDateTime.now()).getSeconds();
    }
    
    public long getTotalDailyUptime() {
        checkResets();
        if (sessionStart != null && !isAfk) {
            return dailyUptime + getCurrentSessionTime();
        }
        return dailyUptime;
    }
    
    public long getTotalWeeklyUptime() {
        checkResets();
        if (sessionStart != null && !isAfk) {
            return weeklyUptime + getCurrentSessionTime();
        }
        return weeklyUptime;
    }
    
    public long getTotalMonthlyUptime() {
        checkResets();
        if (sessionStart != null && !isAfk) {
            return monthlyUptime + getCurrentSessionTime();
        }
        return monthlyUptime;
    }
    
    public String getFormattedDailyUptime() {
        return Util.formatTime(getTotalDailyUptime());
    }
    
    public String getFormattedWeeklyUptime() {
        return Util.formatTime(getTotalWeeklyUptime());
    }
    
    public String getFormattedMonthlyUptime() {
        return Util.formatTime(getTotalMonthlyUptime());
    }
    
    // Getters and setters
    public UUID getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public boolean isOnline() { return sessionStart != null; }
    public boolean isAfk() { return isAfk; }
    public LocalDateTime getLastActivity() { return lastActivity; }
    
    // Database serialization helpers
    public long getDailyUptime() { return dailyUptime; }
    public void setDailyUptime(long dailyUptime) { this.dailyUptime = dailyUptime; }
    public long getWeeklyUptime() { return weeklyUptime; }
    public void setWeeklyUptime(long weeklyUptime) { this.weeklyUptime = weeklyUptime; }
    public long getMonthlyUptime() { return monthlyUptime; }
    public void setMonthlyUptime(long monthlyUptime) { this.monthlyUptime = monthlyUptime; }
    
    public LocalDate getLastDailyReset() { return lastDailyReset; }
    public void setLastDailyReset(LocalDate lastDailyReset) { this.lastDailyReset = lastDailyReset; }
    public int getLastWeekReset() { return lastWeekReset; }
    public void setLastWeekReset(int lastWeekReset) { this.lastWeekReset = lastWeekReset; }
    public int getLastWeekYear() { return lastWeekYear; }
    public void setLastWeekYear(int lastWeekYear) { this.lastWeekYear = lastWeekYear; }
    public int getLastMonthReset() { return lastMonthReset; }
    public void setLastMonthReset(int lastMonthReset) { this.lastMonthReset = lastMonthReset; }
    public int getLastMonthYear() { return lastMonthYear; }
    public void setLastMonthYear(int lastMonthYear) { this.lastMonthYear = lastMonthYear; }
}