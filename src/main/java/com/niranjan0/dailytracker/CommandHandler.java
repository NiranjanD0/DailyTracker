package com.niranjan0.dailytracker;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

    private final DailyTracker plugin;
    private final PlayerDataManager playerDataManager;
    private final DowntimeManager downtimeManager;
    private final DailyTrackerGUI gui;

    public CommandHandler(DailyTracker plugin, PlayerDataManager pdm, DowntimeManager dm, DailyTrackerGUI gui) {
        this.plugin = plugin;
        this.playerDataManager = pdm;
        this.downtimeManager = dm;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("stats")) {
            gui.openGUI(player);
            return true;
        }

        // Placeholder for other subcommands (top, downtime, reload, etc.)
        sender.sendMessage("Subcommands coming soon!");
        return true;
    }
}