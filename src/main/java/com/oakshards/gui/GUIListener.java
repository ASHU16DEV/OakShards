package com.oakshards.gui;

import com.oakshards.OakShards;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class GUIListener implements Listener {

    private final OakShards plugin;

    public GUIListener(OakShards plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof BaseGUI)) return;
        
        event.setCancelled(true);
        
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(event.getInventory())) return;
        
        Player player = (Player) event.getWhoClicked();
        BaseGUI gui = (BaseGUI) holder;
        ItemStack clickedItem = event.getCurrentItem();
        
        gui.handleClick(event.getSlot(), clickedItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BaseGUI) {
            event.setCancelled(true);
        }
    }
}
