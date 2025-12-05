package com.oakshards.gui;

import com.oakshards.OakShards;
import com.oakshards.config.ShopManager;
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

public class ShopEditorGUI extends BaseGUI {

    private static final int ADD_ITEM_SLOT = 49;
    private static final int SETTINGS_SLOT = 50;
    private static final int BACK_SLOT = 45;

    public ShopEditorGUI(OakShards plugin, Player player) {
        super(plugin, player);
        createGUI();
    }

    private void createGUI() {
        int rows = Math.max(plugin.getShopManager().getShopRows() + 1, 6);
        createInventory("&8&lShop Editor", rows);
        
        Map<String, ShopManager.ShopItem> items = plugin.getShopManager().getShopItems();
        for (ShopManager.ShopItem item : items.values()) {
            if (item.getSlot() >= 0 && item.getSlot() < (rows - 1) * 9) {
                ItemStack itemStack = plugin.getShopManager().createEditorItemStack(item);
                inventory.setItem(item.getSlot(), itemStack);
            }
        }
        
        inventory.setItem(ADD_ITEM_SLOT, createItem(Material.LIME_DYE, "&a&lAdd New Item", 
            Arrays.asList("&7Click to add a new shop item")));
        
        inventory.setItem(SETTINGS_SLOT, createItem(Material.COMPARATOR, "&e&lShop Settings",
            Arrays.asList("&7Click to edit shop settings", "", "&7Current rows: &f" + plugin.getShopManager().getShopRows())));
        
        inventory.setItem(BACK_SLOT, createItem(Material.ARROW, "&c&lClose Editor",
            Arrays.asList("&7Click to close the editor")));
        
        fillEmptySlots();
    }

    @Override
    public void handleClick(int slot, ItemStack clickedItem) {
        playClickSound(player);
        
        if (slot == ADD_ITEM_SLOT) {
            player.closeInventory();
            new ItemCreatorGUI(plugin, player).open();
            return;
        }
        
        if (slot == SETTINGS_SLOT) {
            player.closeInventory();
            new ShopSettingsGUI(plugin, player).open();
            return;
        }
        
        if (slot == BACK_SLOT) {
            player.closeInventory();
            return;
        }
        
        if (isFillerItem(clickedItem)) return;
        
        ShopManager.ShopItem shopItem = plugin.getShopManager().getShopItemBySlot(slot);
        if (shopItem != null) {
            player.closeInventory();
            new ItemEditorGUI(plugin, player, shopItem).open();
        }
    }

    public static class ItemCreatorGUI extends BaseGUI {
        private String itemId = "";
        private String material = "STONE";
        private String displayName = "&fNew Item";
        private List<String> lore = new ArrayList<>();
        private int cost = 10;
        private List<String> commands = new ArrayList<>();
        private int slot = 0;
        private int amount = 1;

        public ItemCreatorGUI(OakShards plugin, Player player) {
            super(plugin, player);
            createGUI();
        }

        private void createGUI() {
            createInventory("&8&lCreate Shop Item", 4);
            
            inventory.setItem(10, createItem(Material.NAME_TAG, "&e&lItem ID",
                Arrays.asList("&7Current: &f" + (itemId.isEmpty() ? "Not set" : itemId), "", "&eClick to set")));
            
            inventory.setItem(11, createItem(Material.STONE, "&e&lMaterial",
                Arrays.asList("&7Current: &f" + material, "", "&eClick to set")));
            
            inventory.setItem(12, createItem(Material.OAK_SIGN, "&e&lDisplay Name",
                Arrays.asList("&7Current: &f" + displayName, "", "&eClick to set")));
            
            inventory.setItem(13, createItem(Material.PAPER, "&e&lLore",
                Arrays.asList("&7Lines: &f" + lore.size(), "", "&eClick to add line")));
            
            inventory.setItem(14, createItem(Material.GOLD_INGOT, "&e&lCost",
                Arrays.asList("&7Current: &f" + cost + " shards", "", "&eClick to set")));
            
            inventory.setItem(15, createItem(Material.COMMAND_BLOCK, "&e&lCommands",
                Arrays.asList("&7Commands: &f" + commands.size(), "", "&eClick to add command")));
            
            inventory.setItem(16, createItem(Material.CHEST, "&e&lSlot Position",
                Arrays.asList("&7Current: &f" + slot, "", "&eClick to set")));

            inventory.setItem(19, createItem(Material.IRON_INGOT, "&e&lAmount",
                Arrays.asList("&7Current: &f" + amount, "", "&eClick to set")));
            
            inventory.setItem(31, createItem(Material.LIME_STAINED_GLASS_PANE, "&a&lCreate Item",
                Arrays.asList("&7Click to create the item")));
            
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
                    requestInput("Enter item ID (e.g., diamond_sword):", input -> {
                        itemId = input.toLowerCase().replace(" ", "_");
                        refresh();
                    });
                    break;
                case 11:
                    requestInput("Enter material name (e.g., DIAMOND_SWORD):", input -> {
                        material = input.toUpperCase();
                        refresh();
                    });
                    break;
                case 12:
                    requestInput("Enter display name (with color codes):", input -> {
                        displayName = input;
                        refresh();
                    });
                    break;
                case 13:
                    requestInput("Enter lore line (with color codes):", input -> {
                        lore.add(input);
                        refresh();
                    });
                    break;
                case 14:
                    requestInput("Enter cost in shards:", input -> {
                        try {
                            cost = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            MessageUtils.sendMessage(player, "&cInvalid number!");
                        }
                        refresh();
                    });
                    break;
                case 15:
                    requestInput("Enter command (use {player} for player name):", input -> {
                        commands.add(input);
                        refresh();
                    });
                    break;
                case 16:
                    requestInput("Enter slot number:", input -> {
                        try {
                            this.slot = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            MessageUtils.sendMessage(player, "&cInvalid number!");
                        }
                        refresh();
                    });
                    break;
                case 19:
                    requestInput("Enter item amount:", input -> {
                        try {
                            amount = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            MessageUtils.sendMessage(player, "&cInvalid number!");
                        }
                        refresh();
                    });
                    break;
                case 31:
                    createItem();
                    break;
                case 27:
                    player.closeInventory();
                    new ShopEditorGUI(plugin, player).open();
                    break;
            }
        }

        private void requestInput(String prompt, java.util.function.Consumer<String> callback) {
            player.closeInventory();
            MessageUtils.sendMessage(player, "&a" + prompt);
            MessageUtils.sendMessage(player, "&7Type 'cancel' to cancel.");
            
            ChatListener listener = new ChatListener(plugin, player, callback, this);
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }

        private void refresh() {
            createGUI();
            open();
        }

        private void createItem() {
            if (itemId.isEmpty()) {
                MessageUtils.sendMessage(player, "&cPlease set an item ID first!");
                return;
            }
            
            if (plugin.getShopManager().getShopItem(itemId) != null) {
                MessageUtils.sendMessage(player, "&cAn item with this ID already exists!");
                return;
            }

            ShopManager.ShopItem shopItem = new ShopManager.ShopItem();
            shopItem.setId(itemId);
            shopItem.setMaterial(material);
            shopItem.setDisplayName(displayName);
            shopItem.setLore(lore);
            shopItem.setCost(cost);
            shopItem.setBuyCommands(commands);
            shopItem.setSlot(this.slot);
            shopItem.setAmount(amount);
            
            plugin.getShopManager().addShopItem(itemId, shopItem);
            
            MessageUtils.sendMessage(player, plugin.getMessagesManager().getEditorItemCreated(), new HashMap<>());
            player.closeInventory();
            new ShopEditorGUI(plugin, player).open();
        }
    }

    public static class ItemEditorGUI extends BaseGUI {
        private final ShopManager.ShopItem shopItem;

        public ItemEditorGUI(OakShards plugin, Player player, ShopManager.ShopItem shopItem) {
            super(plugin, player);
            this.shopItem = shopItem;
            createGUI();
        }

        private void createGUI() {
            createInventory("&8&lEdit: " + shopItem.getId(), 5);
            
            inventory.setItem(10, createItem(Material.STONE, "&e&lMaterial",
                Arrays.asList("&7Current: &f" + shopItem.getMaterial(), "", "&eClick to change")));
            
            inventory.setItem(11, createItem(Material.OAK_SIGN, "&e&lDisplay Name",
                Arrays.asList("&7Current: &f" + shopItem.getDisplayName(), "", "&eClick to change")));
            
            List<String> loreLore = new ArrayList<>();
            loreLore.add("&7Lines: &f" + shopItem.getLore().size());
            loreLore.add("");
            if (!shopItem.getLore().isEmpty()) {
                loreLore.add("&7Current:");
                for (String line : shopItem.getLore()) {
                    loreLore.add("  &f" + line);
                }
                loreLore.add("");
            }
            loreLore.add("&eLeft-click to add line");
            loreLore.add("&eRight-click to clear all");
            inventory.setItem(12, createItem(Material.PAPER, "&e&lLore", loreLore));
            
            inventory.setItem(13, createItem(Material.GOLD_INGOT, "&e&lCost",
                Arrays.asList("&7Current: &f" + shopItem.getCost() + " shards", "", "&eClick to change")));
            
            List<String> cmdLore = new ArrayList<>();
            cmdLore.add("&7Commands: &f" + shopItem.getBuyCommands().size());
            cmdLore.add("");
            if (!shopItem.getBuyCommands().isEmpty()) {
                cmdLore.add("&7Current:");
                for (String cmd : shopItem.getBuyCommands()) {
                    cmdLore.add("  &7/" + cmd);
                }
                cmdLore.add("");
            }
            cmdLore.add("&eLeft-click to add command");
            cmdLore.add("&eRight-click to clear all");
            inventory.setItem(14, createItem(Material.COMMAND_BLOCK, "&e&lCommands", cmdLore));
            
            inventory.setItem(15, createItem(Material.CHEST, "&e&lSlot Position",
                Arrays.asList("&7Current: &f" + shopItem.getSlot(), "", "&eClick to change")));

            inventory.setItem(16, createItem(Material.IRON_INGOT, "&e&lAmount",
                Arrays.asList("&7Current: &f" + shopItem.getAmount(), "", "&eClick to change")));
            
            String hasItem = shopItem.hasSerializedItem() ? "&aYes" : "&cNo";
            inventory.setItem(19, createItem(Material.DIAMOND, "&e&lGive Item from Hand",
                Arrays.asList("&7Has saved item: " + hasItem, "", 
                    "&eClick while holding an item", 
                    "&eto save it for this shop item",
                    "",
                    "&7The saved item will be given",
                    "&7to players when they purchase")));
            
            if (shopItem.hasSerializedItem()) {
                inventory.setItem(20, createItem(Material.TNT, "&c&lClear Saved Item",
                    Arrays.asList("&7Click to remove the saved item")));
            }
            
            inventory.setItem(38, createItem(Material.BARRIER, "&c&lDelete Item",
                Arrays.asList("&7Click to delete this item")));
            
            inventory.setItem(36, createItem(Material.ARROW, "&7&lBack",
                Arrays.asList("&7Return to editor")));
            
            fillEmptySlots();
        }

        @Override
        public void handleClick(int slot, ItemStack clickedItem) {
            if (isFillerItem(clickedItem)) return;
            playClickSound(player);

            switch (slot) {
                case 10:
                    requestInput("Enter new material:", input -> {
                        shopItem.setMaterial(input.toUpperCase());
                        plugin.getShopManager().updateShopItem(shopItem.getId(), shopItem);
                        refresh();
                    });
                    break;
                case 11:
                    requestInput("Enter new display name (with color codes):", input -> {
                        shopItem.setDisplayName(input);
                        plugin.getShopManager().updateShopItem(shopItem.getId(), shopItem);
                        refresh();
                    });
                    break;
                case 12:
                    requestInput("Enter lore line to add (with color codes). Type 'clear' to clear all:", input -> {
                        if (input.equalsIgnoreCase("clear")) {
                            shopItem.setLore(new ArrayList<>());
                        } else {
                            List<String> newLore = new ArrayList<>(shopItem.getLore());
                            newLore.add(input);
                            shopItem.setLore(newLore);
                        }
                        plugin.getShopManager().updateShopItem(shopItem.getId(), shopItem);
                        refresh();
                    });
                    break;
                case 13:
                    requestInput("Enter new cost:", input -> {
                        try {
                            shopItem.setCost(Integer.parseInt(input));
                            plugin.getShopManager().updateShopItem(shopItem.getId(), shopItem);
                        } catch (NumberFormatException e) {
                            MessageUtils.sendMessage(player, "&cInvalid number!");
                        }
                        refresh();
                    });
                    break;
                case 14:
                    requestInput("Enter command to add (use %player% for player name). Type 'clear' to clear all:", input -> {
                        if (input.equalsIgnoreCase("clear")) {
                            shopItem.setBuyCommands(new ArrayList<>());
                        } else {
                            List<String> newCmds = new ArrayList<>(shopItem.getBuyCommands());
                            newCmds.add(input);
                            shopItem.setBuyCommands(newCmds);
                        }
                        plugin.getShopManager().updateShopItem(shopItem.getId(), shopItem);
                        refresh();
                    });
                    break;
                case 15:
                    requestInput("Enter new slot:", input -> {
                        try {
                            shopItem.setSlot(Integer.parseInt(input));
                            plugin.getShopManager().updateShopItem(shopItem.getId(), shopItem);
                        } catch (NumberFormatException e) {
                            MessageUtils.sendMessage(player, "&cInvalid number!");
                        }
                        refresh();
                    });
                    break;
                case 16:
                    requestInput("Enter new amount:", input -> {
                        try {
                            shopItem.setAmount(Integer.parseInt(input));
                            plugin.getShopManager().updateShopItem(shopItem.getId(), shopItem);
                        } catch (NumberFormatException e) {
                            MessageUtils.sendMessage(player, "&cInvalid number!");
                        }
                        refresh();
                    });
                    break;
                case 19:
                    ItemStack handItem = player.getInventory().getItemInMainHand();
                    if (handItem == null || handItem.getType().isAir()) {
                        MessageUtils.sendMessage(player, "&cYou must hold an item in your hand!");
                    } else {
                        String serialized = ShopManager.serializeItemStack(handItem);
                        if (serialized != null) {
                            shopItem.setSerializedItem(serialized);
                            shopItem.setMaterial(handItem.getType().name());
                            shopItem.setAmount(handItem.getAmount());
                            plugin.getShopManager().updateShopItem(shopItem.getId(), shopItem);
                            MessageUtils.sendMessage(player, "&aItem saved! This item will be given on purchase.");
                        } else {
                            MessageUtils.sendMessage(player, "&cFailed to save item!");
                        }
                    }
                    refresh();
                    break;
                case 20:
                    if (shopItem.hasSerializedItem()) {
                        shopItem.setSerializedItem(null);
                        plugin.getShopManager().updateShopItem(shopItem.getId(), shopItem);
                        MessageUtils.sendMessage(player, "&cSaved item removed.");
                    }
                    refresh();
                    break;
                case 38:
                    plugin.getShopManager().removeShopItem(shopItem.getId());
                    MessageUtils.sendMessage(player, plugin.getMessagesManager().getEditorItemRemoved(), new HashMap<>());
                    player.closeInventory();
                    new ShopEditorGUI(plugin, player).open();
                    break;
                case 36:
                    player.closeInventory();
                    new ShopEditorGUI(plugin, player).open();
                    break;
            }
        }

        private void requestInput(String prompt, java.util.function.Consumer<String> callback) {
            player.closeInventory();
            MessageUtils.sendMessage(player, "&a" + prompt);
            MessageUtils.sendMessage(player, "&7Type 'cancel' to cancel.");
            
            ChatListener listener = new ChatListener(plugin, player, callback, this);
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }

        private void refresh() {
            createGUI();
            open();
        }
    }

    public static class ShopSettingsGUI extends BaseGUI {

        public ShopSettingsGUI(OakShards plugin, Player player) {
            super(plugin, player);
            createGUI();
        }

        private void createGUI() {
            createInventory("&8&lShop Settings", 3);
            
            inventory.setItem(11, createItem(Material.CHEST, "&e&lShop Rows",
                Arrays.asList("&7Current: &f" + plugin.getShopManager().getShopRows() + " rows", "", "&eClick to change")));
            
            inventory.setItem(13, createItem(Material.NAME_TAG, "&e&lShop Title",
                Arrays.asList("&7Current: &f" + plugin.getShopManager().getShopTitle(), "", "&eClick to change")));
            
            inventory.setItem(15, createItem(Material.ARROW, "&c&lBack",
                Arrays.asList("&7Return to editor")));
            
            fillEmptySlots();
        }

        @Override
        public void handleClick(int slot, ItemStack clickedItem) {
            if (isFillerItem(clickedItem)) return;
            playClickSound(player);

            switch (slot) {
                case 11:
                    requestInput("Enter new shop rows (1-6):", input -> {
                        try {
                            int rows = Integer.parseInt(input);
                            if (rows >= 1 && rows <= 6) {
                                plugin.getShopManager().setShopRows(rows);
                            } else {
                                MessageUtils.sendMessage(player, "&cRows must be between 1 and 6!");
                            }
                        } catch (NumberFormatException e) {
                            MessageUtils.sendMessage(player, "&cInvalid number!");
                        }
                        refresh();
                    });
                    break;
                case 13:
                    requestInput("Enter new shop title:", input -> {
                        plugin.getShopManager().setShopTitle(input);
                        refresh();
                    });
                    break;
                case 15:
                    player.closeInventory();
                    new ShopEditorGUI(plugin, player).open();
                    break;
            }
        }

        private void requestInput(String prompt, java.util.function.Consumer<String> callback) {
            player.closeInventory();
            MessageUtils.sendMessage(player, "&a" + prompt);
            MessageUtils.sendMessage(player, "&7Type 'cancel' to cancel.");
            
            ChatListener listener = new ChatListener(plugin, player, callback, this);
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }

        private void refresh() {
            createGUI();
            open();
        }
    }

    private static class ChatListener implements Listener {
        private final OakShards plugin;
        private final Player player;
        private final java.util.function.Consumer<String> callback;
        private final BaseGUI returnGUI;

        public ChatListener(OakShards plugin, Player player, java.util.function.Consumer<String> callback, BaseGUI returnGUI) {
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
                    MessageUtils.sendMessage(player, plugin.getMessagesManager().getEditorCancelled(), new HashMap<>());
                    returnGUI.open();
                });
                return;
            }
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                callback.accept(message);
            });
        }
    }
}
