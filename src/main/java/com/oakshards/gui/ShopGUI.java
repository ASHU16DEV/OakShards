package com.oakshards.gui;

import com.oakshards.OakShards;
import com.oakshards.config.ShopManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ShopGUI extends BaseGUI {

    public ShopGUI(OakShards plugin, Player player) {
        super(plugin, player);
        createGUI();
    }

    private void createGUI() {
        String title = plugin.getShopManager().getShopTitle();
        int rows = plugin.getShopManager().getShopRows();
        
        createInventory(title, rows);
        
        Map<String, ShopManager.ShopItem> items = plugin.getShopManager().getShopItems();
        for (ShopManager.ShopItem item : items.values()) {
            if (item.getSlot() >= 0 && item.getSlot() < inventory.getSize()) {
                ItemStack itemStack = plugin.getShopManager().createShopItemStack(item, player);
                inventory.setItem(item.getSlot(), itemStack);
            }
        }
        
        fillEmptySlots();
    }

    @Override
    public void handleClick(int slot, ItemStack clickedItem) {
        if (isFillerItem(clickedItem)) return;
        
        playClickSound(player);
        
        ShopManager.ShopItem shopItem = plugin.getShopManager().getShopItemBySlot(slot);
        if (shopItem == null) return;
        
        String permission = shopItem.getRequiredPermission();
        if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
            com.oakshards.utils.MessageUtils.sendMessage(player, 
                plugin.getMessagesManager().getShopNoPermission(), 
                new java.util.HashMap<>());
            return;
        }
        
        if (plugin.getShopManager().isConfirmEnabled()) {
            new ConfirmGUI(plugin, player, shopItem).open();
        } else {
            processPurchase(shopItem);
        }
    }

    public void processPurchase(ShopManager.ShopItem item) {
        long cost = item.getCost();
        long balance = plugin.getDatabaseManager().getBalance(player);

        java.util.Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put("cost", String.valueOf(cost));
        placeholders.put("item", com.oakshards.utils.ColorUtils.stripColor(item.getDisplayName()));
        placeholders.put("balance", String.valueOf(balance));

        if (balance < cost) {
            com.oakshards.utils.MessageUtils.sendMessage(player, 
                plugin.getMessagesManager().getShopNotEnoughShards(), 
                placeholders);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (!hasInventorySpace(player)) {
            com.oakshards.utils.MessageUtils.sendMessage(player, 
                plugin.getMessagesManager().getMessage("shop.inventory-full"), 
                placeholders);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        plugin.getDatabaseManager().removeShards(player.getUniqueId(), cost);

        for (String command : item.getBuyCommands()) {
            String processedCommand = command.replace("%player%", player.getName())
                                             .replace("{player}", player.getName());
            org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(), processedCommand);
        }

        if (item.hasSerializedItem()) {
            ItemStack giveItem = ShopManager.deserializeItemStack(item.getSerializedItem());
            if (giveItem != null) {
                player.getInventory().addItem(giveItem.clone());
            }
        }

        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        
        com.oakshards.utils.MessageUtils.sendMessage(player, 
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
