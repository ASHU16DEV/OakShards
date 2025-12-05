package com.oakshards.commands;

import com.oakshards.OakShards;
import com.oakshards.database.DatabaseManager;
import com.oakshards.utils.ColorUtils;
import com.oakshards.utils.MessageUtils;
import com.oakshards.utils.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class ShardsCommand implements CommandExecutor, TabCompleter {

    private final OakShards plugin;

    public ShardsCommand(OakShards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getPlayerOnly());
                return true;
            }
            
            Player player = (Player) sender;
            
            if (!player.hasPermission("oakshards.player.balance")) {
                MessageUtils.sendMessage(player, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
                return true;
            }
            
            long balance = plugin.getDatabaseManager().getBalance(player);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("balance", NumberFormatter.format(balance));
            
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getBalanceSelf(), placeholders);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "top":
                return handleTop(sender, args);
            default:
                if (sender.hasPermission("oakshards.player.balance.others")) {
                    return handleOther(sender, args[0]);
                }
                break;
        }

        return true;
    }

    private boolean handleTop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("oakshards.player.top")) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            }
            return true;
        }

        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        int perPage = 10;
        List<Map.Entry<String, Long>> topPlayers = plugin.getDatabaseManager().getTopPlayers(100);
        
        if (topPlayers.isEmpty()) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getTopEmpty(), new HashMap<>());
            } else {
                sender.sendMessage(ColorUtils.colorize(plugin.getMessagesManager().getTopEmpty()));
            }
            return true;
        }

        int totalPages = (int) Math.ceil((double) topPlayers.size() / perPage);
        page = Math.max(1, Math.min(page, totalPages));

        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, topPlayers.size());

        sender.sendMessage(ColorUtils.colorize(plugin.getMessagesManager().getTopHeader()));
        
        for (int i = start; i < end; i++) {
            Map.Entry<String, Long> entry = topPlayers.get(i);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("position", String.valueOf(i + 1));
            placeholders.put("player", entry.getKey());
            placeholders.put("balance", NumberFormatter.format(entry.getValue()));
            
            sender.sendMessage(MessageUtils.replacePlaceholders(
                plugin.getMessagesManager().getTopEntry(), placeholders));
        }

        Map<String, String> pageInfo = new HashMap<>();
        pageInfo.put("page", String.valueOf(page));
        pageInfo.put("total_pages", String.valueOf(totalPages));
        sender.sendMessage(MessageUtils.replacePlaceholders(
            plugin.getMessagesManager().getTopPageInfo(), pageInfo));
        
        sender.sendMessage(ColorUtils.colorize(plugin.getMessagesManager().getTopFooter()));

        return true;
    }

    private boolean handleOther(CommandSender sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        DatabaseManager.PlayerData data;
        
        if (target != null && target.isOnline()) {
            data = plugin.getDatabaseManager().getPlayerData(target);
        } else {
            data = plugin.getDatabaseManager().getOfflinePlayerData(targetName);
        }

        if (data == null) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getPlayerNotFound(), new HashMap<>());
            } else {
                sender.sendMessage(ColorUtils.colorize(plugin.getMessagesManager().getPlayerNotFound()));
            }
            return true;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", data.getUsername());
        placeholders.put("balance", NumberFormatter.format(data.getBalance()));

        if (sender instanceof Player) {
            MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getBalanceOther(), placeholders);
        } else {
            sender.sendMessage(MessageUtils.replacePlaceholders(plugin.getMessagesManager().getBalanceOther(), placeholders));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("top");
            if (sender.hasPermission("oakshards.player.balance.others")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("top")) {
            for (int i = 1; i <= 10; i++) {
                completions.add(String.valueOf(i));
            }
        }

        String input = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(input));
        
        return completions;
    }
}
