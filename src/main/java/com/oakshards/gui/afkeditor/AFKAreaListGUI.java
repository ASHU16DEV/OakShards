package com.oakshards.gui.afkeditor;

import com.oakshards.OakShards;
import com.oakshards.afk.AFKArea;
import com.oakshards.gui.BaseGUI;
import com.oakshards.utils.ColorUtils;
import com.oakshards.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AFKAreaListGUI extends BaseGUI {

    private static final int CREATE_SLOT = 49;
    private static final int CLOSE_SLOT = 45;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 36;

    public AFKAreaListGUI(OakShards plugin, Player player) {
        super(plugin, player);
        createGUI();
    }

    private void createGUI() {
        createInventory("&8&lAFK Area Editor", 6);
        
        Map<String, AFKArea> areas = plugin.getAFKAreasManager().getAFKAreas();
        List<AFKArea> areaList = new ArrayList<>(areas.values());
        
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, areaList.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            AFKArea area = areaList.get(i);
            int slot = i - startIndex;
            
            Material material = area.isEnabled() ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Status: " + (area.isEnabled() ? "&aEnabled" : "&cDisabled"));
            lore.add("&7World: &f" + area.getWorld().getName());
            lore.add("&7Interval: &f" + area.getIntervalSeconds() + "s");
            lore.add("&7Shards: &f" + area.getShardsPerInterval() + " per interval");
            lore.add("");
            lore.add("&eClick to edit this area");
            
            inventory.setItem(slot, createItem(material, "&a&l" + area.getName(), lore));
        }
        
        if (currentPage > 0) {
            inventory.setItem(48, createItem(Material.ARROW, "&a&lPrevious Page",
                Arrays.asList("&7Click to go to previous page")));
        }
        
        if (endIndex < areaList.size()) {
            inventory.setItem(50, createItem(Material.ARROW, "&a&lNext Page",
                Arrays.asList("&7Click to go to next page")));
        }
        
        inventory.setItem(CREATE_SLOT, createItem(Material.LIME_DYE, "&a&lCreate New Area",
            Arrays.asList("&7Click to create a new AFK area", "", "&7You will need to set positions first")));
        
        inventory.setItem(CLOSE_SLOT, createItem(Material.BARRIER, "&c&lClose",
            Arrays.asList("&7Click to close the editor")));
        
        fillEmptySlots();
    }

    @Override
    public void handleClick(int slot, ItemStack clickedItem) {
        if (isFillerItem(clickedItem)) return;
        playClickSound(player);
        
        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }
        
        if (slot == CREATE_SLOT) {
            player.closeInventory();
            requestAreaName();
            return;
        }
        
        if (slot == 48 && currentPage > 0) {
            currentPage--;
            createGUI();
            return;
        }
        
        if (slot == 50) {
            currentPage++;
            createGUI();
            return;
        }
        
        if (slot < ITEMS_PER_PAGE) {
            Map<String, AFKArea> areas = plugin.getAFKAreasManager().getAFKAreas();
            List<AFKArea> areaList = new ArrayList<>(areas.values());
            int index = currentPage * ITEMS_PER_PAGE + slot;
            
            if (index < areaList.size()) {
                AFKArea area = areaList.get(index);
                player.closeInventory();
                new AFKAreaEditorGUI(plugin, player, area).open();
            }
        }
    }

    private void requestAreaName() {
        MessageUtils.sendMessage(player, "&a&lCreate New AFK Area");
        MessageUtils.sendMessage(player, "&7Enter a name for the new area:");
        MessageUtils.sendMessage(player, "&7Type 'cancel' to cancel.");
        
        CreateAreaListener listener = new CreateAreaListener(plugin, player, this);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    private static class CreateAreaListener implements Listener {
        private final OakShards plugin;
        private final Player player;
        private final AFKAreaListGUI returnGUI;

        public CreateAreaListener(OakShards plugin, Player player, AFKAreaListGUI returnGUI) {
            this.plugin = plugin;
            this.player = player;
            this.returnGUI = returnGUI;
        }

        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            if (!event.getPlayer().equals(player)) return;
            
            event.setCancelled(true);
            HandlerList.unregisterAll(this);
            
            String message = event.getMessage();
            
            if (message.equalsIgnoreCase("cancel")) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtils.sendMessage(player, "&cArea creation cancelled.");
                    returnGUI.createGUI();
                    returnGUI.open();
                });
                return;
            }
            
            String areaName = message.toLowerCase().replace(" ", "_");
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (plugin.getAFKAreasManager().areaExists(areaName)) {
                    MessageUtils.sendMessage(player, "&cAn area with this name already exists!");
                    returnGUI.createGUI();
                    returnGUI.open();
                    return;
                }
                
                new AFKAreaCreatorGUI(plugin, player, areaName).open();
            });
        }
    }
}
