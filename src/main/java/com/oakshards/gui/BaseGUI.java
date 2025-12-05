package com.oakshards.gui;

import com.oakshards.OakShards;
import com.oakshards.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class BaseGUI implements InventoryHolder {

    protected final OakShards plugin;
    protected final Player player;
    protected Inventory inventory;

    public BaseGUI(OakShards plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    protected void createInventory(String title, int rows) {
        inventory = Bukkit.createInventory(this, rows * 9, ColorUtils.colorize(title));
    }

    protected ItemStack createItem(Material material, String name, List<String> lore) {
        return createItem(material, 1, name, lore);
    }

    protected ItemStack createItem(Material material, int amount, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtils.colorize(name));
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(ColorUtils.colorize(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createFillerItem() {
        String materialStr = plugin.getShopManager().getFillerMaterial();
        Material material;
        try {
            material = Material.valueOf(materialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.BLACK_STAINED_GLASS_PANE;
        }
        
        return createItem(material, plugin.getShopManager().getFillerName(), null);
    }

    protected void fillEmptySlots() {
        if (!plugin.getShopManager().isFillerEnabled()) return;
        
        ItemStack filler = createFillerItem();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    protected void playClickSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    protected boolean isFillerItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return true;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return true;
        
        String fillerName = ColorUtils.colorize(plugin.getShopManager().getFillerName());
        return meta.getDisplayName().equals(fillerName);
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public abstract void handleClick(int slot, ItemStack clickedItem);
}
