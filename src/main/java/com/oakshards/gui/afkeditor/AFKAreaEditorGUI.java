package com.oakshards.gui.afkeditor;

import com.oakshards.OakShards;
import com.oakshards.afk.AFKArea;
import com.oakshards.gui.BaseGUI;
import com.oakshards.gui.ConfirmGUI;
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

public class AFKAreaEditorGUI extends BaseGUI {

    private final AFKArea area;

    public AFKAreaEditorGUI(OakShards plugin, Player player, AFKArea area) {
        super(plugin, player);
        this.area = area;
        createGUI();
    }

    private void createGUI() {
        createInventory("&8&lEdit: " + area.getName(), 5);
        
        Material enabledMaterial = area.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE;
        String enabledStatus = area.isEnabled() ? "&aEnabled" : "&cDisabled";
        inventory.setItem(10, createItem(enabledMaterial, "&e&lToggle Status",
            Arrays.asList("&7Current: " + enabledStatus, "", "&eClick to toggle")));
        
        String pos1Str = area.getPos1().getBlockX() + ", " + area.getPos1().getBlockY() + ", " + area.getPos1().getBlockZ();
        inventory.setItem(11, createItem(Material.RED_WOOL, "&e&lPosition 1",
            Arrays.asList("&7Current: &f" + pos1Str, "", "&eClick to set to your location")));
        
        String pos2Str = area.getPos2().getBlockX() + ", " + area.getPos2().getBlockY() + ", " + area.getPos2().getBlockZ();
        inventory.setItem(12, createItem(Material.BLUE_WOOL, "&e&lPosition 2",
            Arrays.asList("&7Current: &f" + pos2Str, "", "&eClick to set to your location")));
        
        inventory.setItem(14, createItem(Material.CLOCK, "&e&lInterval (seconds)",
            Arrays.asList("&7Current: &f" + area.getIntervalSeconds() + " seconds", "", "&eClick to change")));
        
        inventory.setItem(15, createItem(Material.EMERALD, "&e&lShards Per Interval",
            Arrays.asList("&7Current: &f" + area.getShardsPerInterval() + " shards", "", "&eClick to change")));
        
        inventory.setItem(16, createItem(Material.COMPASS, "&e&lWorld",
            Arrays.asList("&7Current: &f" + area.getWorld().getName(), "", "&7(Read-only)")));
        
        inventory.setItem(19, createItem(Material.OAK_SIGN, "&e&lEntry Title",
            Arrays.asList("&7Current:", "&f" + ColorUtils.colorize(area.getEntryTitle()), "", "&eClick to change")));
        
        inventory.setItem(20, createItem(Material.OAK_SIGN, "&e&lEntry Subtitle",
            Arrays.asList("&7Current:", "&f" + ColorUtils.colorize(area.getEntrySubtitle()), "", "&eClick to change")));
        
        inventory.setItem(21, createItem(Material.DARK_OAK_SIGN, "&e&lLeave Title",
            Arrays.asList("&7Current:", "&f" + ColorUtils.colorize(area.getLeaveTitle()), "", "&eClick to change")));
        
        inventory.setItem(22, createItem(Material.DARK_OAK_SIGN, "&e&lLeave Subtitle",
            Arrays.asList("&7Current:", "&f" + ColorUtils.colorize(area.getLeaveSubtitle()), "", "&eClick to change")));
        
        inventory.setItem(24, createItem(Material.PAPER, "&e&lAction Bar Format",
            Arrays.asList("&7Current:", "&f" + ColorUtils.colorize(area.getActionBarFormat()), "", "&eClick to change")));
        
        inventory.setItem(40, createItem(Material.BARRIER, "&c&lDelete Area",
            Arrays.asList("&7Click to delete this area", "&cThis action cannot be undone!")));
        
        inventory.setItem(36, createItem(Material.ARROW, "&7&lBack",
            Arrays.asList("&7Return to area list")));
        
        inventory.setItem(44, createItem(Material.LIME_DYE, "&a&lSave & Back",
            Arrays.asList("&7Save changes and return")));
        
        fillEmptySlots();
    }

    @Override
    public void handleClick(int slot, ItemStack clickedItem) {
        if (isFillerItem(clickedItem)) return;
        playClickSound(player);
        
        switch (slot) {
            case 10:
                area.setEnabled(!area.isEnabled());
                plugin.getAFKAreasManager().updateArea(area);
                createGUI();
                break;
            case 11:
                area.setPos1(player.getLocation().clone());
                plugin.getAFKAreasManager().updateArea(area);
                MessageUtils.sendMessage(player, "&aPosition 1 updated!");
                createGUI();
                break;
            case 12:
                area.setPos2(player.getLocation().clone());
                plugin.getAFKAreasManager().updateArea(area);
                MessageUtils.sendMessage(player, "&aPosition 2 updated!");
                createGUI();
                break;
            case 14:
                requestInput("Enter new interval in seconds:", input -> {
                    try {
                        int value = Integer.parseInt(input);
                        if (value > 0) {
                            area.setIntervalSeconds(value);
                            plugin.getAFKAreasManager().updateArea(area);
                            MessageUtils.sendMessage(player, "&aInterval updated!");
                        } else {
                            MessageUtils.sendMessage(player, "&cInterval must be greater than 0!");
                        }
                    } catch (NumberFormatException e) {
                        MessageUtils.sendMessage(player, "&cInvalid number!");
                    }
                    refresh();
                });
                break;
            case 15:
                requestInput("Enter shards per interval:", input -> {
                    try {
                        int value = Integer.parseInt(input);
                        if (value > 0) {
                            area.setShardsPerInterval(value);
                            plugin.getAFKAreasManager().updateArea(area);
                            MessageUtils.sendMessage(player, "&aShards per interval updated!");
                        } else {
                            MessageUtils.sendMessage(player, "&cShards must be greater than 0!");
                        }
                    } catch (NumberFormatException e) {
                        MessageUtils.sendMessage(player, "&cInvalid number!");
                    }
                    refresh();
                });
                break;
            case 19:
                requestInput("Enter new entry title (with color codes):", input -> {
                    area.setEntryTitle(input);
                    plugin.getAFKAreasManager().updateArea(area);
                    MessageUtils.sendMessage(player, "&aEntry title updated!");
                    refresh();
                });
                break;
            case 20:
                requestInput("Enter new entry subtitle (with color codes):", input -> {
                    area.setEntrySubtitle(input);
                    plugin.getAFKAreasManager().updateArea(area);
                    MessageUtils.sendMessage(player, "&aEntry subtitle updated!");
                    refresh();
                });
                break;
            case 21:
                requestInput("Enter new leave title (with color codes):", input -> {
                    area.setLeaveTitle(input);
                    plugin.getAFKAreasManager().updateArea(area);
                    MessageUtils.sendMessage(player, "&aLeave title updated!");
                    refresh();
                });
                break;
            case 22:
                requestInput("Enter new leave subtitle (with color codes):", input -> {
                    area.setLeaveSubtitle(input);
                    plugin.getAFKAreasManager().updateArea(area);
                    MessageUtils.sendMessage(player, "&aLeave subtitle updated!");
                    refresh();
                });
                break;
            case 24:
                requestInput("Enter new action bar format (use %time_formatted% for time):", input -> {
                    area.setActionBarFormat(input);
                    plugin.getAFKAreasManager().updateArea(area);
                    MessageUtils.sendMessage(player, "&aAction bar format updated!");
                    refresh();
                });
                break;
            case 40:
                player.closeInventory();
                new DeleteConfirmGUI(plugin, player, area).open();
                break;
            case 36:
            case 44:
                plugin.getAFKAreasManager().save();
                plugin.getAFKManager().reloadAreas();
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

    private static class InputListener implements Listener {
        private final OakShards plugin;
        private final Player player;
        private final java.util.function.Consumer<String> callback;
        private final AFKAreaEditorGUI returnGUI;

        public InputListener(OakShards plugin, Player player, java.util.function.Consumer<String> callback, AFKAreaEditorGUI returnGUI) {
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

    private static class DeleteConfirmGUI extends BaseGUI {
        private final AFKArea area;

        public DeleteConfirmGUI(OakShards plugin, Player player, AFKArea area) {
            super(plugin, player);
            this.area = area;
            createGUI();
        }

        private void createGUI() {
            createInventory("&c&lDelete " + area.getName() + "?", 3);
            
            inventory.setItem(11, createItem(Material.LIME_STAINED_GLASS_PANE, "&a&lConfirm Delete",
                Arrays.asList("&7Click to permanently delete", "&c" + area.getName())));
            
            inventory.setItem(15, createItem(Material.RED_STAINED_GLASS_PANE, "&c&lCancel",
                Arrays.asList("&7Click to cancel")));
            
            fillEmptySlots();
        }

        @Override
        public void handleClick(int slot, ItemStack clickedItem) {
            if (isFillerItem(clickedItem)) return;
            playClickSound(player);
            
            if (slot == 11) {
                plugin.getAFKAreasManager().removeArea(area.getName());
                plugin.getAFKManager().reloadAreas();
                MessageUtils.sendMessage(player, "&cAFK Area '&f" + area.getName() + "&c' deleted.");
                player.closeInventory();
                new AFKAreaListGUI(plugin, player).open();
            } else if (slot == 15) {
                player.closeInventory();
                new AFKAreaEditorGUI(plugin, player, area).open();
            }
        }
    }
}
