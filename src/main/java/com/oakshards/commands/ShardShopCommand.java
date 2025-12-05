package com.oakshards.commands;

import com.oakshards.OakShards;
import com.oakshards.gui.ShopGUI;
import com.oakshards.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class ShardShopCommand implements CommandExecutor {

    private final OakShards plugin;

    public ShardShopCommand(OakShards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("oakshards.player.shop")) {
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getNoPermission(), new HashMap<>());
            return true;
        }

        MessageUtils.sendMessage(player, plugin.getMessagesManager().getShopOpen(), new HashMap<>());
        new ShopGUI(plugin, player).open();

        return true;
    }
}
