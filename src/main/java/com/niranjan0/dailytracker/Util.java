package com.niranjan0.dailytracker;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return hours + "h " + minutes + "m " + seconds + "s";
    }

    public static ItemStack createGuiItem(String materialName, String displayName, List<String> lorePlaceholders) {
        Material material = Material.matchMaterial(materialName);
        if (material == null) material = Material.PAPER;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            List<String> lore = new ArrayList<>();
            for (String line : lorePlaceholders) lore.add(line);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}