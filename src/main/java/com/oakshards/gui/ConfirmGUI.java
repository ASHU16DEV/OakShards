package com.oakshards.gui;

import com.oakshards.OakShards;
import com.oakshards.config.ShopManager;
import com.oakshards.utils.ColorUtils;
import com.oakshards.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ConfirmGUI extends BaseGUI {

    private final ShopManager.ShopItem shopItem;
    private final int confirmSlot;
    private final int cancelSlot;

    public ConfirmGUI(OakShards plugin, Player player, ShopManager.ShopItem shopItem) {
        super(plugin, player);
        this.shopItem = shopItem;
        this.confirmSlot = plugin.getShopManager().getConfirmSlot();
        this.cancelSlot = plugin.getShopManager().getCancelSlot();
        createGUI();
    }

    private void createGUI() {
        String title = plugin.getShopManager().getConfirmTitle();
        int rows = plugin.getShopManager().getConfirmRows();
        
        createInventory(title, rows);
        
        ItemStack displayItem = plugin.getShopManager().createShopItemStack(shopItem);
        inventory.setItem(plugin.getShopManager().getItemDisplaySlot(), displayItem);
        
        Material confirmMaterial;
        try {
            confirmMaterial = Material.valueOf(plugin.getShopManager().getConfirmMaterial().toUpperCase());
        } catch (IllegalArgumentException e) {
            confirmMaterial = Material.LIME_STAINED_GLASS_PANE;
        }
        
        ItemStack confirmItem = createItem(confirmMaterial, 
            plugin.getShopManager().getConfirmName(),
            plugin.getShopManager().getConfirmLore());
        inventory.setItem(confirmSlot, confirmItem);
        
        Material cancelMaterial;
        try {
            cancelMaterial = Material.valueOf(plugin.getShopManager().getCancelMaterial().toUpperCase());
        } catch (IllegalArgumentException e) {
            cancelMaterial = Material.RED_STAINED_GLASS_PANE;
        }
        
        ItemStack cancelItem = createItem(cancelMaterial,
            plugin.getShopManager().getCancelName(),
            plugin.getShopManager().getCancelLore());
        inventory.setItem(cancelSlot, cancelItem);
        
        fillEmptySlots();
    }

    @Override
    public void handleClick(int slot, ItemStack clickedItem) {
        if (isFillerItem(clickedItem)) return;
        
        playClickSound(player);
        
        if (slot == confirmSlot) {
            processPurchase();
        } else if (slot == cancelSlot) {
            player.closeInventory();
            new ShopGUI(plugin, player).open();
        }
    }

    private void processPurchase() {
        long cost = shopItem.getCost();
        long balance = plugin.getDatabaseManager().getBalance(player);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("cost", String.valueOf(cost));
        placeholders.put("item", ColorUtils.stripColor(shopItem.getDisplayName()));
        placeholders.put("balance", String.valueOf(balance));

        if (balance < cost) {
            MessageUtils.sendMessage(player, 
                plugin.getMessagesManager().getShopNotEnoughShards(), 
                placeholders);
            player.closeInventory();
            return;
        }

        if (!hasInventorySpace(player)) {
            MessageUtils.sendMessage(player, 
                plugin.getMessagesManager().getMessage("shop.inventory-full"), 
                placeholders);
            player.closeInventory();
            return;
        }

        plugin.getDatabaseManager().removeShards(player.getUniqueId(), cost);

        for (String command : shopItem.getBuyCommands()) {
            String processedCommand = command.replace("{player}", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }

        MessageUtils.sendMessage(player, 
            plugin.getMessagesManager().getShopPurchaseSuccess(), 
            placeholders);
        
        player.closeInventory();
    }

    private boolean hasInventorySpace(Player player) {
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) {
                return true;
            }
        }
        return false;
    }
}
