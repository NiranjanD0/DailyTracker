package com.niranjan0.dailytracker.gui;

import com.niranjan0.dailytracker.DailyTracker;
import com.niranjan0.dailytracker.data.PlayerData;
import com.niranjan0.dailytracker.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class StatsGUI implements Listener {
    
    private final DailyTracker plugin;
    private final Player targetPlayer;
    private final Map<Player, Inventory> openGuis = new HashMap<>();
    
    public StatsGUI(DailyTracker plugin, Player targetPlayer) {
        this.plugin = plugin;
        this.targetPlayer = targetPlayer;
        
        // Register this as a listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public void open(Player viewer) {
        PlayerData data = plugin.getDataManager().getPlayerData(targetPlayer.getUniqueId());
        if (data == null) {
            viewer.sendMessage("§cPlayer data not found!");
            return;
        }
        
        ConfigurationSection guiConfig = plugin.getConfigManager().getGuiConfig().getConfigurationSection("stats-gui");
        if (guiConfig == null) {
            viewer.sendMessage("§cGUI configuration not found!");
            return;
        }
        
        String title = Util.colorize(guiConfig.getString("title", "&6&lDaily Tracker Stats"));
        title = replacePlaceholders(title, data);
        
        int size = guiConfig.getInt("size", 54);
        Inventory inv = Bukkit.createInventory(null, size, title);
        
        // Add filler items
        ConfigurationSection fillerConfig = guiConfig.getConfigurationSection("items.filler");
        if (fillerConfig != null) {
            Material fillerMaterial = Material.valueOf(fillerConfig.getString("material", "BLACK_STAINED_GLASS_PANE"));
            String fillerName = Util.colorize(fillerConfig.getString("name", " "));
            ItemStack filler = Util.createItem(fillerMaterial, fillerName, null);
            
            List<Integer> fillerSlots = fillerConfig.getIntegerList("slots");
            for (int slot : fillerSlots) {
                if (slot >= 0 && slot < size) {
                    inv.setItem(slot, filler);
                }
            }
        }
        
        // Add specific items
        addGuiItem(inv, guiConfig, "uptime", data);
        addGuiItem(inv, guiConfig, "refresh", data);
        addGuiItem(inv, guiConfig, "close", data);
        addGuiItem(inv, guiConfig, "daily-stats", data);
        addGuiItem(inv, guiConfig, "weekly-stats", data);
        addGuiItem(inv, guiConfig, "monthly-stats", data);
        
        openGuis.put(viewer, inv);
        viewer.openInventory(inv);
    }
    
    private void addGuiItem(Inventory inv, ConfigurationSection guiConfig, String itemKey, PlayerData data) {
        ConfigurationSection itemConfig = guiConfig.getConfigurationSection("items." + itemKey);
        if (itemConfig == null) return;
        
        int slot = itemConfig.getInt("slot", -1);
        if (slot < 0 || slot >= inv.getSize()) return;
        
        Material material = Material.valueOf(itemConfig.getString("material", "STONE"));
        String name = Util.colorize(itemConfig.getString("name", ""));
        List<String> lore = itemConfig.getStringList("lore");
        
        // Replace placeholders
        name = replacePlaceholders(name, data);
        lore = lore.stream().map(line -> replacePlaceholders(Util.colorize(line), data)).toList();
        
        ItemStack item = Util.createItem(material, name, lore);
        inv.setItem(slot, item);
    }
    
    private String replacePlaceholders(String text, PlayerData data) {
        if (text == null || data == null) return text;
        
        return text
            .replace("%dat_uptime_day%", data.getFormattedDailyUptime())
            .replace("%dat_uptime_day_raw%", String.valueOf(data.getTotalDailyUptime()))
            .replace("%dat_uptime_week%", data.getFormattedWeeklyUptime())
            .replace("%dat_uptime_week_raw%", String.valueOf(data.getTotalWeeklyUptime()))
            .replace("%dat_uptime_month%", data.getFormattedMonthlyUptime())
            .replace("%dat_uptime_month_raw%", String.valueOf(data.getTotalMonthlyUptime()));
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null || !openGuis.containsKey(player)) return;
        
        if (!clickedInv.equals(openGuis.get(player))) return;
        
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        ConfigurationSection guiConfig = plugin.getConfigManager().getGuiConfig().getConfigurationSection("stats-gui");
        if (guiConfig == null) return;
        
        int slot = event.getSlot();
        
        // Check which item was clicked based on slot
        if (slot == guiConfig.getInt("items.refresh.slot", 50)) {
            // Refresh button clicked
            openGuis.remove(player);
            open(player);
        } else if (slot == guiConfig.getInt("items.close.slot", 54)) {
            // Close button clicked
            player.closeInventory();
            openGuis.remove(player);
        }
    }
    
    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            openGuis.remove(player);
        }
    }
}