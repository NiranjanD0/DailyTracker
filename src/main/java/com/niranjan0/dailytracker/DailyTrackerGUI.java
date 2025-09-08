package com.niranjan0.dailytracker;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class DailyTrackerGUI {

    private final DailyTracker plugin;
    private FileConfiguration guiConfig;

    public DailyTrackerGUI(DailyTracker plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveResource("gui.yml", false);
        guiConfig = plugin.getConfig(); // in real version, load gui.yml separately
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Daily Tracker Stats");

        // Example: read slots 46 and 52 from gui.yml
        ItemStack daily = Util.createGuiItem("PAPER", "Daily Uptime", List.of("%dailytracker_uptime_day%"));
        ItemStack monthly = Util.createGuiItem("CLOCK", "Monthly Uptime", List.of("%dailytracker_uptime_month%"));

        inv.setItem(46, daily);
        inv.setItem(52, monthly);

        player.openInventory(inv);
    }
}