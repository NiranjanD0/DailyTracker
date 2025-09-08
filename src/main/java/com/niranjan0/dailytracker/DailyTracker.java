package com.niranjan0.dailytracker;

import org.bukkit.plugin.java.JavaPlugin;

public class DailyTracker extends JavaPlugin {

    private static DailyTracker instance;
    private PlayerDataManager playerDataManager;
    private DowntimeManager downtimeManager;
    private CommandHandler commandHandler;
    private DailyTrackerGUI gui;
    private PlaceholderHook placeholderHook;

    @Override
    public void onEnable() {
        instance = this;

        // Load config
        saveDefaultConfig();

        // Initialize managers
        playerDataManager = new PlayerDataManager(this);
        downtimeManager = new DowntimeManager(this);
        gui = new DailyTrackerGUI(this);
        commandHandler = new CommandHandler(this, playerDataManager, downtimeManager, gui);

        // Register commands
        getCommand("dup").setExecutor(commandHandler);

        // Hook PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderHook = new PlaceholderHook(this, playerDataManager, downtimeManager);
            placeholderHook.register();
        }

        getLogger().info("DailyTracker enabled!");
    }

    @Override
    public void onDisable() {
        // Save all data
        playerDataManager.saveAll();
        downtimeManager.save();
        getLogger().info("DailyTracker disabled!");
    }

    public static DailyTracker getInstance() {
        return instance;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public DowntimeManager getDowntimeManager() {
        return downtimeManager;
    }

    public DailyTrackerGUI getGui() {
        return gui;
    }
}