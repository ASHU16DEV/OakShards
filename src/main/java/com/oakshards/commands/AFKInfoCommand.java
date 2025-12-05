package com.oakshards.commands;

import com.oakshards.OakShards;
import com.oakshards.afk.AFKArea;
import com.oakshards.utils.MessageUtils;
import com.oakshards.utils.NumberFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AFKInfoCommand implements CommandExecutor {

    private final OakShards plugin;

    public AFKInfoCommand(OakShards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("oakshards.player.afkinfo")) {
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            return true;
        }

        AFKArea currentArea = plugin.getAFKManager().getCurrentArea(player);
        Map<String, String> placeholders = new HashMap<>();

        if (currentArea != null) {
            placeholders.put("area", currentArea.getName());
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getAFKInfoInArea(), placeholders);
            
            int timeRemaining = plugin.getAFKManager().getTimeRemaining(player);
            placeholders.put("time_remaining", String.valueOf(timeRemaining));
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getAFKInfoNextShard(), placeholders);
            
            placeholders.put("amount", String.valueOf(currentArea.getShardsPerInterval()));
            placeholders.put("interval", String.valueOf(currentArea.getIntervalSeconds()));
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getAFKInfoEarningRate(), placeholders);
        } else {
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getAFKInfoNotInArea(), placeholders);
        }

        placeholders.put("balance", NumberFormatter.format(plugin.getDatabaseManager().getBalance(player)));
        MessageUtils.sendMessage(player, plugin.getMessagesManager().getBalanceSelf(), placeholders);

        return true;
    }
}
