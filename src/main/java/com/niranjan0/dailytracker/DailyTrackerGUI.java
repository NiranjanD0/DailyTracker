package com.niranjan0.dailytracker;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public class DailyTrackerGUI {

    private final DailyTracker plugin;
    private FileConfiguration guiConfig;
    private File guiFile;

    public DailyTrackerGUI(DailyTracker plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        guiFile = new File(plugin.getDataFolder(), "gui.yml");
        if (!guiFile.exists()) {
            plugin.saveResource("gui.yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
    }

    public void openGUI(Player player) {
        String title = guiConfig.getString("gui.title", "Daily Tracker Stats").replace("&", "§");
        int size = guiConfig.getInt("gui.size", 54);
        Inventory inv = Bukkit.createInventory(null, size, title);

        guiConfig.getConfigurationSection("items").getKeys(false).forEach(key -> {
            int slot = Integer.parseInt(key);
            String material = guiConfig.getString("items." + key + ".material", "PAPER");
            String displayName = guiConfig.getString("items." + key + ".display-name", "Item").replace("&", "§");
            List<String> lore = guiConfig.getStringList("items." + key + ".lore");
            lore.replaceAll(s -> s.replace("&", "§"));

            // keep old %dailytracker_...% placeholders intact
            ItemStack item = Util.createGuiItem(material, displayName, lore);
            inv.setItem(slot, item);
        });

        player.openInventory(inv);
    }

    public void reloadGUI() {
        loadConfig();
    }
}
