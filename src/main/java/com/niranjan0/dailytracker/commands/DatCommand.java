package com.niranjan0.dailytracker.commands;

import com.niranjan0.dailytracker.DailyTracker;
import com.niranjan0.dailytracker.data.PlayerData;
import com.niranjan0.dailytracker.gui.StatsGUI;
import com.niranjan0.dailytracker.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.*;

public class DatCommand implements CommandExecutor, TabCompleter {
    
    private final DailyTracker plugin;
    private final Set<String> pendingConfirmations = new HashSet<>();
    
    public DatCommand(DailyTracker plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("dailytracker.use")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "stats" -> handleStatsCommand(sender, args);
            case "top" -> handleTopCommand(sender, args);
            case "history" -> handleHistoryCommand(sender, args);
            case "downtime" -> handleDowntimeCommand(sender, args);
            case "reload" -> handleReloadCommand(sender);
            default -> sendHelpMessage(sender);
        }
        
        return true;
    }
    
    private void handleStatsCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return;
        }
        
        Player targetPlayer = player;
        
        if (args.length > 1) {
            if (!sender.hasPermission("dailytracker.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return;
            }
            
            targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
                return;
            }
        }
        
        // Open GUI
        StatsGUI gui = new StatsGUI(plugin, targetPlayer);
        gui.open(player);
    }
    
    private void handleTopCommand(CommandSender sender, String[] args) {
        String period = args.length > 1 ? args[1].toLowerCase() : "daily";
        
        if (!period.equals("daily") && !period.equals("weekly") && !period.equals("monthly")) {
            sender.sendMessage("§cInvalid period! Use: daily, weekly, or monthly");
            return;
        }
        
        List<PlayerData> topPlayers = plugin.getDataManager().getTopPlayers(period, 10);
        
        sender.sendMessage("§6§l=== Top " + Util.capitalizeWords(period) + " Players ===");
        
        if (topPlayers.isEmpty()) {
            sender.sendMessage("§7No players found.");
            return;
        }
        
        for (int i = 0; i < topPlayers.size(); i++) {
            PlayerData data = topPlayers.get(i);
            long uptime = switch (period) {
                case "weekly" -> data.getTotalWeeklyUptime();
                case "monthly" -> data.getTotalMonthlyUptime();
                default -> data.getTotalDailyUptime();
            };
            
            sender.sendMessage(String.format("§6%s. §f%s §7- §a%s", 
                Util.getOrdinal(i + 1), 
                data.getPlayerName(), 
                Util.formatTime(uptime)));
        }
    }
    
    private void handleHistoryCommand(CommandSender sender, String[] args) {
        // For simplicity, this will show current stats
        // In a full implementation, you would store historical data
        sender.sendMessage("§7History feature is coming soon!");
    }
    
    private void handleDowntimeCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dailytracker.admin.downtime")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        if (args.length > 1 && args[1].equalsIgnoreCase("reset")) {
            if (args.length > 2 && args[2].equalsIgnoreCase("confirm")) {
                String confirmKey = sender.getName() + ":downtime_reset";
                if (!pendingConfirmations.contains(confirmKey)) {
                    sender.sendMessage("§cNo reset confirmation pending!");
                    return;
                }
                
                pendingConfirmations.remove(confirmKey);
                plugin.getDataManager().resetDowntime();
                sender.sendMessage(plugin.getConfigManager().getMessage("downtime-reset-success"));
            } else {
                String confirmKey = sender.getName() + ":downtime_reset";
                pendingConfirmations.add(confirmKey);
                
                // Remove confirmation after 30 seconds
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    pendingConfirmations.remove(confirmKey);
                }, 600L);
                
                sender.sendMessage(plugin.getConfigManager().getMessage("downtime-reset-confirm"));
            }
        } else {
            long totalDowntime = plugin.getDataManager().getTotalDowntime();
            String startDate = plugin.getConfigManager().getConfig().getString("monthly-downtime-start-date", "Unknown");
            
            sender.sendMessage("§6§l=== Server Downtime ===");
            sender.sendMessage("§7Since: §f" + startDate);
            sender.sendMessage("§7Total downtime: §c" + Util.formatTime(totalDowntime));
            sender.sendMessage("§7Raw seconds: §f" + totalDowntime);
        }
    }
    
    private void handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("dailytracker.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        
        plugin.reload();
        sender.sendMessage(plugin.getConfigManager().getMessage("config-reloaded"));
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§6§l=== DailyTracker Commands ===");
        sender.sendMessage("§7/dat stats §f- View your statistics");
        
        if (sender.hasPermission("dailytracker.admin")) {
            sender.sendMessage("§7/dat stats <player> §f- View another player's stats");
        }
        
        sender.sendMessage("§7/dat top <daily|weekly|monthly> §f- View top players");
        sender.sendMessage("§7/dat history <day|week|month> <date> §f- View uptime history");
        
        if (sender.hasPermission("dailytracker.admin.downtime")) {
            sender.sendMessage("§7/dat downtime §f- View server downtime");
            sender.sendMessage("§7/dat downtime reset §f- Reset downtime tracking");
        }
        
        if (sender.hasPermission("dailytracker.admin")) {
            sender.sendMessage("§7/dat reload §f- Reload configuration");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("stats", "top", "history");
            
            if (sender.hasPermission("dailytracker.admin.downtime")) {
                subCommands = new ArrayList<>(subCommands);
                subCommands.add("downtime");
            }
            
            if (sender.hasPermission("dailytracker.admin")) {
                subCommands = new ArrayList<>(subCommands);
                subCommands.add("reload");
            }
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    suggestions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "stats" -> {
                    if (sender.hasPermission("dailytracker.admin")) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                                suggestions.add(player.getName());
                            }
                        }
                    }
                }
                case "top" -> {
                    List<String> periods = Arrays.asList("daily", "weekly", "monthly");
                    for (String period : periods) {
                        if (period.toLowerCase().startsWith(args[1].toLowerCase())) {
                            suggestions.add(period);
                        }
                    }
                }
                case "history" -> {
                    List<String> periods = Arrays.asList("day", "week", "month");
                    for (String period : periods) {
                        if (period.toLowerCase().startsWith(args[1].toLowerCase())) {
                            suggestions.add(period);
                        }
                    }
                }
                case "downtime" -> {
                    if (sender.hasPermission("dailytracker.admin.downtime") && "reset".startsWith(args[1].toLowerCase())) {
                        suggestions.add("reset");
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("downtime") && args[1].equalsIgnoreCase("reset")) {
            if (sender.hasPermission("dailytracker.admin.downtime") && "confirm".startsWith(args[2].toLowerCase())) {
                suggestions.add("confirm");
            }
        }
        
        return suggestions;
    }
}