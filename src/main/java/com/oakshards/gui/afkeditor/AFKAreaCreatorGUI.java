package com.oakshards.gui.afkeditor;

import com.oakshards.OakShards;
import com.oakshards.gui.BaseGUI;
import com.oakshards.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AFKAreaCreatorGUI extends BaseGUI {

    private final String areaName;
    private Location pos1;
    private Location pos2;
    private int intervalSeconds = 60;
    private int shardsPerInterval = 1;

    public AFKAreaCreatorGUI(OakShards plugin, Player player, String areaName) {
        super(plugin, player);
        this.areaName = areaName;
        createGUI();
    }

    private void createGUI() {
        createInventory("&8&lCreate: " + areaName, 4);
        
        String pos1Status = pos1 != null ? 
            "&a" + pos1.getBlockX() + ", " + pos1.getBlockY() + ", " + pos1.getBlockZ() : "&cNot set";
        String pos2Status = pos2 != null ? 
            "&a" + pos2.getBlockX() + ", " + pos2.getBlockY() + ", " + pos2.getBlockZ() : "&cNot set";
        
        inventory.setItem(10, createItem(Material.RED_WOOL, "&e&lPosition 1",
            Arrays.asList("&7Current: " + pos1Status, "", "&eClick to set to your location")));
        
        inventory.setItem(11, createItem(Material.BLUE_WOOL, "&e&lPosition 2",
            Arrays.asList("&7Current: " + pos2Status, "", "&eClick to set to your location")));
        
        inventory.setItem(13, createItem(Material.CLOCK, "&e&lInterval (seconds)",
            Arrays.asList("&7Current: &f" + intervalSeconds + " seconds", "", "&eClick to change")));
        
        inventory.setItem(14, createItem(Material.EMERALD, "&e&lShards Per Interval",
            Arrays.asList("&7Current: &f" + shardsPerInterval + " shards", "", "&eClick to change")));
        
        Material createMaterial = (pos1 != null && pos2 != null) ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
        List<String> createLore = new ArrayList<>();
        if (pos1 == null || pos2 == null) {
            createLore.add("&cSet both positions first!");
        } else {
            createLore.add("&7Click to create the area");
        }
        
        inventory.setItem(31, createItem(createMaterial, "&a&lCreate Area", createLore));
        
        inventory.setItem(27, createItem(Material.RED_STAINED_GLASS_PANE, "&c&lCancel",
            Arrays.asList("&7Click to cancel")));
        
        fillEmptySlots();
    }

    @Override
    public void handleClick(int slot, ItemStack clickedItem) {
        if (isFillerItem(clickedItem)) return;
        playClickSound(player);
        
        switch (slot) {
            case 10:
                pos1 = player.getLocation().clone();
                MessageUtils.sendMessage(player, "&aPosition 1 set to your current location!");
                createGUI();
                break;
            case 11:
                pos2 = player.getLocation().clone();
                MessageUtils.sendMessage(player, "&aPosition 2 set to your current location!");
                createGUI();
                break;
            case 13:
                requestInput("Enter interval in seconds:", input -> {
                    try {
                        int value = Integer.parseInt(input);
                        if (value > 0) {
                            intervalSeconds = value;
                        } else {
                            MessageUtils.sendMessage(player, "&cInterval must be greater than 0!");
                        }
                    } catch (NumberFormatException e) {
                        MessageUtils.sendMessage(player, "&cInvalid number!");
                    }
                    refresh();
                });
                break;
            case 14:
                requestInput("Enter shards per interval:", input -> {
                    try {
                        int value = Integer.parseInt(input);
                        if (value > 0) {
                            shardsPerInterval = value;
                        } else {
                            MessageUtils.sendMessage(player, "&cShards must be greater than 0!");
                        }
                    } catch (NumberFormatException e) {
                        MessageUtils.sendMessage(player, "&cInvalid number!");
                    }
                    refresh();
                });
                break;
            case 31:
                if (pos1 != null && pos2 != null) {
                    createArea();
                } else {
                    MessageUtils.sendMessage(player, "&cPlease set both positions first!");
                }
                break;
            case 27:
                player.closeInventory();
                new AFKAreaListGUI(plugin, player).open();
                break;
        }
    }

    private void requestInput(String prompt, java.util.function.Consumer<String> callback) {
        player.closeInventory();
        MessageUtils.sendMessage(player, "&a" + prompt);
        MessageUtils.sendMessage(player, "&7Type 'cancel' to cancel.");
        
        InputListener listener = new InputListener(plugin, player, callback, this);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }

    private void refresh() {
        createGUI();
        open();
    }

    private void createArea() {
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            MessageUtils.sendMessage(player, "&cBoth positions must be in the same world!");
            return;
        }
        
        plugin.getAFKAreasManager().createAreaWithSettings(areaName, pos1, pos2, intervalSeconds, shardsPerInterval);
        plugin.getAFKManager().reloadAreas();
        
        MessageUtils.sendMessage(player, "&aAFK Area '&f" + areaName + "&a' created successfully!");
        player.closeInventory();
        new AFKAreaListGUI(plugin, player).open();
    }

    private static class InputListener implements Listener {
        private final OakShards plugin;
        private final Player player;
        private final java.util.function.Consumer<String> callback;
        private final AFKAreaCreatorGUI returnGUI;

        public InputListener(OakShards plugin, Player player, java.util.function.Consumer<String> callback, AFKAreaCreatorGUI returnGUI) {
            this.plugin = plugin;
            this.player = player;
            this.callback = callback;
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
                    MessageUtils.sendMessage(player, "&cCancelled.");
                    returnGUI.refresh();
                });
                return;
            }
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                callback.accept(message);
            });
        }
    }
}
