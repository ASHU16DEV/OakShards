package com.oakshards.commands;

import com.oakshards.OakShards;
import com.oakshards.afk.AFKArea;
import com.oakshards.database.DatabaseManager;
import com.oakshards.gui.ShopEditorGUI;
import com.oakshards.gui.afkeditor.AFKAreaListGUI;
import com.oakshards.utils.ColorUtils;
import com.oakshards.utils.MessageUtils;
import com.oakshards.utils.NumberFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class OakShardsCommand implements CommandExecutor, TabCompleter {

    private final OakShards plugin;
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();

    public OakShardsCommand(OakShards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "give":
                return handleGive(sender, args);
            case "take":
                return handleTake(sender, args);
            case "set":
                return handleSet(sender, args);
            case "setpos1":
                return handleSetPos1(sender);
            case "setpos2":
                return handleSetPos2(sender);
            case "createarea":
                return handleCreateArea(sender, args);
            case "removeafkarea":
            case "removearea":
                return handleRemoveArea(sender, args);
            case "listafkarea":
            case "listareas":
                return handleListAreas(sender);
            case "shopedit":
                return handleShopEdit(sender);
            case "areaedit":
                return handleAreaEdit(sender);
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize("&8&m---------&r &a&lOakShards Commands &8&m---------"));
        sender.sendMessage(ColorUtils.colorize("&a/shards &8- &7View your shard balance"));
        sender.sendMessage(ColorUtils.colorize("&a/shards top [page] &8- &7View top shard holders"));
        sender.sendMessage(ColorUtils.colorize("&a/shardshop &8- &7Open the shard shop"));
        sender.sendMessage(ColorUtils.colorize("&a/afkinfo &8- &7View your AFK status"));
        
        if (sender.hasPermission("oakshards.admin.reload")) {
            sender.sendMessage(ColorUtils.colorize("&a/oakshards reload &8- &7Reload all configs"));
        }
        if (sender.hasPermission("oakshards.admin.give")) {
            sender.sendMessage(ColorUtils.colorize("&a/oakshards give <player> <amount> &8- &7Give shards"));
        }
        if (sender.hasPermission("oakshards.admin.take")) {
            sender.sendMessage(ColorUtils.colorize("&a/oakshards take <player> <amount> &8- &7Take shards"));
        }
        if (sender.hasPermission("oakshards.admin.set")) {
            sender.sendMessage(ColorUtils.colorize("&a/oakshards set <player> <amount> &8- &7Set balance"));
        }
        if (sender.hasPermission("oakshards.admin.afkarea")) {
            sender.sendMessage(ColorUtils.colorize("&a/oakshards setpos1 &8- &7Set AFK area position 1"));
            sender.sendMessage(ColorUtils.colorize("&a/oakshards setpos2 &8- &7Set AFK area position 2"));
            sender.sendMessage(ColorUtils.colorize("&a/oakshards createarea <name> &8- &7Create AFK area"));
            sender.sendMessage(ColorUtils.colorize("&a/oakshards removearea <name> &8- &7Remove AFK area"));
            sender.sendMessage(ColorUtils.colorize("&a/oakshards listareas &8- &7List all AFK areas"));
        }
        if (sender.hasPermission("oakshards.admin.shop.edit")) {
            sender.sendMessage(ColorUtils.colorize("&a/oakshards shopedit &8- &7Open shop editor"));
        }
        if (sender.hasPermission("oakshards.admin.afkarea")) {
            sender.sendMessage(ColorUtils.colorize("&a/oakshards areaedit &8- &7Open AFK area editor GUI"));
        }
        sender.sendMessage(ColorUtils.colorize("&8&m-----------------------------------------"));
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("oakshards.admin.reload")) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            }
            return true;
        }

        plugin.reloadAllConfigs();
        
        if (sender instanceof Player) {
            MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getReloaded(), new HashMap<>());
        } else {
            sender.sendMessage(ColorUtils.colorize(plugin.getMessagesManager().getReloaded()));
        }
        
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("oakshards.admin.give")) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            }
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /oakshards give <player> <amount>"));
            return true;
        }

        String targetName = args[1];
        long amount;
        
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getInvalidNumber(), new HashMap<>());
            } else {
                sender.sendMessage(ColorUtils.colorize(plugin.getMessagesManager().getInvalidNumber()));
            }
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        
        if (target != null && target.isOnline()) {
            plugin.getDatabaseManager().addShards(target.getUniqueId(), amount);
        } else {
            DatabaseManager.PlayerData data = plugin.getDatabaseManager().getOfflinePlayerData(targetName);
            if (data == null) {
                if (sender instanceof Player) {
                    MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getPlayerNotFound(), new HashMap<>());
                } else {
                    sender.sendMessage(ColorUtils.colorize(plugin.getMessagesManager().getPlayerNotFound()));
                }
                return true;
            }
            plugin.getDatabaseManager().addOfflineShards(targetName, amount);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", targetName);
        placeholders.put("amount", NumberFormatter.format(amount));

        if (sender instanceof Player) {
            MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getAdminGive(), placeholders);
        } else {
            sender.sendMessage(MessageUtils.replacePlaceholders(plugin.getMessagesManager().getAdminGive(), placeholders));
        }

        return true;
    }

    private boolean handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("oakshards.admin.take")) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            }
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /oakshards take <player> <amount>"));
            return true;
        }

        String targetName = args[1];
        long amount;
        
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getInvalidNumber(), new HashMap<>());
            } else {
                sender.sendMessage(ColorUtils.colorize(plugin.getMessagesManager().getInvalidNumber()));
            }
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        boolean success;
        
        if (target != null && target.isOnline()) {
            success = plugin.getDatabaseManager().removeShards(target.getUniqueId(), amount);
        } else {
            success = plugin.getDatabaseManager().removeOfflineShards(targetName, amount);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", targetName);
        placeholders.put("amount", NumberFormatter.format(amount));

        if (!success) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getAdminNotEnoughShards(), placeholders);
            } else {
                sender.sendMessage(MessageUtils.replacePlaceholders(plugin.getMessagesManager().getAdminNotEnoughShards(), placeholders));
            }
            return true;
        }

        if (sender instanceof Player) {
            MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getAdminTake(), placeholders);
        } else {
            sender.sendMessage(MessageUtils.replacePlaceholders(plugin.getMessagesManager().getAdminTake(), placeholders));
        }

        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("oakshards.admin.set")) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            }
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /oakshards set <player> <amount>"));
            return true;
        }

        String targetName = args[1];
        long amount;
        
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getInvalidNumber(), new HashMap<>());
            } else {
                sender.sendMessage(ColorUtils.colorize(plugin.getMessagesManager().getInvalidNumber()));
            }
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        
        if (target != null && target.isOnline()) {
            plugin.getDatabaseManager().setBalance(target.getUniqueId(), amount);
        } else {
            DatabaseManager.PlayerData data = plugin.getDatabaseManager().getOfflinePlayerData(targetName);
            if (data == null) {
                if (sender instanceof Player) {
                    MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getPlayerNotFound(), new HashMap<>());
                } else {
                    sender.sendMessage(ColorUtils.colorize(plugin.getMessagesManager().getPlayerNotFound()));
                }
                return true;
            }
            plugin.getDatabaseManager().setOfflineBalance(targetName, amount);
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", targetName);
        placeholders.put("amount", NumberFormatter.format(amount));

        if (sender instanceof Player) {
            MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getAdminSet(), placeholders);
        } else {
            sender.sendMessage(MessageUtils.replacePlaceholders(plugin.getMessagesManager().getAdminSet(), placeholders));
        }

        return true;
    }

    private boolean handleSetPos1(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("oakshards.admin.afkarea")) {
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            return true;
        }

        pos1Map.put(player.getUniqueId(), player.getLocation().clone());
        MessageUtils.sendMessage(player, plugin.getMessagesManager().getPos1Set(), new HashMap<>());
        
        return true;
    }

    private boolean handleSetPos2(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("oakshards.admin.afkarea")) {
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            return true;
        }

        pos2Map.put(player.getUniqueId(), player.getLocation().clone());
        MessageUtils.sendMessage(player, plugin.getMessagesManager().getPos2Set(), new HashMap<>());
        
        return true;
    }

    private boolean handleCreateArea(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("oakshards.admin.afkarea")) {
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtils.colorize("&cUsage: /oakshards createarea <name>"));
            return true;
        }

        String areaName = args[1].toLowerCase();
        
        if (plugin.getAFKAreasManager().areaExists(areaName)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("area", areaName);
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getAFKAreaAlreadyExists(), placeholders);
            return true;
        }

        Location pos1 = pos1Map.get(player.getUniqueId());
        Location pos2 = pos2Map.get(player.getUniqueId());

        if (pos1 == null || pos2 == null) {
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getNeedPositions(), new HashMap<>());
            return true;
        }

        if (!pos1.getWorld().equals(pos2.getWorld())) {
            player.sendMessage(ColorUtils.colorize("&cBoth positions must be in the same world!"));
            return true;
        }

        plugin.getAFKAreasManager().createArea(areaName, pos1, pos2);
        plugin.getAFKManager().reloadAreas();
        
        pos1Map.remove(player.getUniqueId());
        pos2Map.remove(player.getUniqueId());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("area", areaName);
        MessageUtils.sendMessage(player, plugin.getMessagesManager().getAFKAreaCreated(), placeholders);
        
        return true;
    }

    private boolean handleRemoveArea(CommandSender sender, String[] args) {
        if (!sender.hasPermission("oakshards.admin.afkarea")) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            }
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /oakshards removearea <name>"));
            return true;
        }

        String areaName = args[1].toLowerCase();
        
        if (!plugin.getAFKAreasManager().areaExists(areaName)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("area", areaName);
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getAFKAreaNotFound(), placeholders);
            } else {
                sender.sendMessage(MessageUtils.replacePlaceholders(plugin.getMessagesManager().getAFKAreaNotFound(), placeholders));
            }
            return true;
        }

        plugin.getAFKAreasManager().removeArea(areaName);
        plugin.getAFKManager().reloadAreas();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("area", areaName);
        if (sender instanceof Player) {
            MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getAFKAreaRemoved(), placeholders);
        } else {
            sender.sendMessage(MessageUtils.replacePlaceholders(plugin.getMessagesManager().getAFKAreaRemoved(), placeholders));
        }
        
        return true;
    }

    private boolean handleListAreas(CommandSender sender) {
        if (!sender.hasPermission("oakshards.admin.afkarea")) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            }
            return true;
        }

        Map<String, AFKArea> areas = plugin.getAFKAreasManager().getAFKAreas();
        
        if (areas.isEmpty()) {
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getAFKListEmpty(), new HashMap<>());
            } else {
                sender.sendMessage(ColorUtils.colorize(plugin.getMessagesManager().getAFKListEmpty()));
            }
            return true;
        }

        if (sender instanceof Player) {
            MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getAFKListHeader(), new HashMap<>());
        } else {
            sender.sendMessage(ColorUtils.colorize(plugin.getMessagesManager().getAFKListHeader()));
        }
        
        for (AFKArea area : areas.values()) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("area", area.getName());
            placeholders.put("world", area.getWorld().getName());
            
            if (sender instanceof Player) {
                MessageUtils.sendMessage((Player) sender, plugin.getMessagesManager().getAFKListEntry(), placeholders);
            } else {
                sender.sendMessage(MessageUtils.replacePlaceholders(plugin.getMessagesManager().getAFKListEntry(), placeholders));
            }
        }
        
        return true;
    }

    private boolean handleShopEdit(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("oakshards.admin.shop.edit")) {
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            return true;
        }

        new ShopEditorGUI(plugin, player).open();
        
        return true;
    }

    private boolean handleAreaEdit(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("oakshards.admin.afkarea")) {
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            return true;
        }

        new AFKAreaListGUI(plugin, player).open();
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("oakshards.admin.reload")) completions.add("reload");
            if (sender.hasPermission("oakshards.admin.give")) completions.add("give");
            if (sender.hasPermission("oakshards.admin.take")) completions.add("take");
            if (sender.hasPermission("oakshards.admin.set")) completions.add("set");
            if (sender.hasPermission("oakshards.admin.afkarea")) {
                completions.add("setpos1");
                completions.add("setpos2");
                completions.add("createarea");
                completions.add("removearea");
                completions.add("listareas");
            }
            if (sender.hasPermission("oakshards.admin.shop.edit")) completions.add("shopedit");
            if (sender.hasPermission("oakshards.admin.afkarea")) completions.add("areaedit");
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            
            if (sub.equals("give") || sub.equals("take") || sub.equals("set")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            } else if (sub.equals("removearea")) {
                completions.addAll(plugin.getAFKAreasManager().getAFKAreas().keySet());
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("take") || sub.equals("set")) {
                completions.add("10");
                completions.add("50");
                completions.add("100");
                completions.add("500");
                completions.add("1000");
            }
        }

        String input = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(input));
        
        return completions;
    }
}
