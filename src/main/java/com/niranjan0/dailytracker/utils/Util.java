package com.niranjan0.dailytracker.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class Util {
    
    /**
     * Formats seconds into a human-readable string (e.g., "2h 30m 45s")
     */
    public static String formatTime(long seconds) {
        if (seconds < 0) return "0s";
        
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        StringBuilder sb = new StringBuilder();
        
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(secs).append("s");
        
        return sb.toString().trim();
    }
    
    /**
     * Formats seconds into a detailed string (e.g., "2 hours, 30 minutes, 45 seconds")
     */
    public static String formatTimeDetailed(long seconds) {
        if (seconds < 0) return "0 seconds";
        
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        StringBuilder sb = new StringBuilder();
        
        if (hours > 0) {
            sb.append(hours).append(hours == 1 ? " hour" : " hours");
            if (minutes > 0 || secs > 0) sb.append(", ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(minutes == 1 ? " minute" : " minutes");
            if (secs > 0) sb.append(", ");
        }
        if (secs > 0 || sb.length() == 0) {
            sb.append(secs).append(secs == 1 ? " second" : " seconds");
        }
        
        return sb.toString();
    }
    
    /**
     * Creates an ItemStack with the given material, name, and lore
     */
    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(colorize(name));
            }
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore.stream().map(Util::colorize).toList());
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Creates a player head with the given player name
     */
    public static ItemStack createPlayerHead(String playerName, String displayName, List<String> lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null) {
            meta.setOwner(playerName);
            if (displayName != null) {
                meta.setDisplayName(colorize(displayName));
            }
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore.stream().map(Util::colorize).toList());
            }
            head.setItemMeta(meta);
        }
        
        return head;
    }
    
    /**
     * Colorizes a string by replacing & with §
     */
    public static String colorize(String text) {
        if (text == null) return null;
        return text.replace("&", "§");
    }
    
    /**
     * Removes color codes from a string
     */
    public static String stripColor(String text) {
        if (text == null) return null;
        return text.replaceAll("§[0-9a-fk-or]", "");
    }
    
    /**
     * Validates if a string is a valid date in YYYY-MM-DD format
     */
    public static boolean isValidDate(String dateString) {
        try {
            java.time.LocalDate.parse(dateString);
            return true;
        } catch (java.time.format.DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * Gets the ordinal suffix for a number (1st, 2nd, 3rd, etc.)
     */
    public static String getOrdinal(int number) {
        if (number % 100 >= 11 && number % 100 <= 13) {
            return number + "th";
        }
        
        return switch (number % 10) {
            case 1 -> number + "st";
            case 2 -> number + "nd";
            case 3 -> number + "rd";
            default -> number + "th";
        };
    }
    
    /**
     * Capitalizes the first letter of each word in a string
     */
    public static String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) return text;
        
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");
            
            String word = words[i];
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
            }
        }
        
        return result.toString();
    }
}