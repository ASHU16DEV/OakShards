package com.oakshards.config;

import com.oakshards.OakShards;
import com.oakshards.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShopManager {

    private final OakShards plugin;
    private FileConfiguration shopConfig;
    private File shopFile;
    private final Map<String, ShopItem> shopItems = new HashMap<>();

    public ShopManager(OakShards plugin) {
        this.plugin = plugin;
        loadShopConfig();
    }

    private void loadShopConfig() {
        shopFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }
        shopConfig = YamlConfiguration.loadConfiguration(shopFile);
        loadItems();
    }

    public void reload() {
        shopConfig = YamlConfiguration.loadConfiguration(shopFile);
        loadItems();
    }

    private void loadItems() {
        shopItems.clear();
        ConfigurationSection itemsSection = shopConfig.getConfigurationSection("shop.items");
        if (itemsSection == null) return;

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection == null) continue;

            ShopItem item = new ShopItem();
            item.setId(key);
            item.setSlot(itemSection.getInt("slot", 0));
            item.setMaterial(itemSection.getString("material", "STONE"));
            item.setAmount(itemSection.getInt("amount", 1));
            item.setDisplayName(itemSection.getString("display-name", key));
            item.setLore(itemSection.getStringList("lore"));
            item.setCost(itemSection.getInt("cost", 1));
            item.setBuyCommands(itemSection.getStringList("buy-commands"));
            item.setRequiredPermission(itemSection.getString("required-permission", ""));
            item.setGiveItems(itemSection.getStringList("give-items"));
            item.setSerializedItem(itemSection.getString("serialized-item", null));

            shopItems.put(key, item);
        }
    }

    public void save() {
        ConfigurationSection itemsSection = shopConfig.createSection("shop.items");
        
        for (Map.Entry<String, ShopItem> entry : shopItems.entrySet()) {
            ShopItem item = entry.getValue();
            ConfigurationSection itemSection = itemsSection.createSection(entry.getKey());
            
            itemSection.set("slot", item.getSlot());
            itemSection.set("material", item.getMaterial());
            itemSection.set("amount", item.getAmount());
            itemSection.set("display-name", item.getDisplayName());
            itemSection.set("lore", item.getLore());
            itemSection.set("cost", item.getCost());
            itemSection.set("buy-commands", item.getBuyCommands());
            if (item.getRequiredPermission() != null && !item.getRequiredPermission().isEmpty()) {
                itemSection.set("required-permission", item.getRequiredPermission());
            }
            if (item.getGiveItems() != null && !item.getGiveItems().isEmpty()) {
                itemSection.set("give-items", item.getGiveItems());
            }
            if (item.getSerializedItem() != null && !item.getSerializedItem().isEmpty()) {
                itemSection.set("serialized-item", item.getSerializedItem());
            }
        }

        try {
            shopConfig.save(shopFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save shop.yml: " + e.getMessage());
        }
    }

    public String getShopTitle() {
        return shopConfig.getString("shop.title", "&8&lShard Shop");
    }

    public int getShopRows() {
        return shopConfig.getInt("shop.rows", 4);
    }

    public boolean isFillerEnabled() {
        return shopConfig.getBoolean("shop.filler.enabled", true);
    }

    public String getFillerMaterial() {
        return shopConfig.getString("shop.filler.material", "BLACK_STAINED_GLASS_PANE");
    }

    public String getFillerName() {
        return shopConfig.getString("shop.filler.name", " ");
    }

    public boolean isConfirmEnabled() {
        return shopConfig.getBoolean("confirm-gui.enabled", true);
    }

    public String getConfirmTitle() {
        return shopConfig.getString("confirm-gui.title", "&8Confirm Purchase");
    }

    public int getConfirmRows() {
        return shopConfig.getInt("confirm-gui.rows", 3);
    }

    public int getConfirmSlot() {
        return shopConfig.getInt("confirm-gui.confirm.slot", 11);
    }

    public String getConfirmMaterial() {
        return shopConfig.getString("confirm-gui.confirm.material", "LIME_STAINED_GLASS_PANE");
    }

    public String getConfirmName() {
        return shopConfig.getString("confirm-gui.confirm.name", "&a&lConfirm");
    }

    public List<String> getConfirmLore() {
        return shopConfig.getStringList("confirm-gui.confirm.lore");
    }

    public int getCancelSlot() {
        return shopConfig.getInt("confirm-gui.cancel.slot", 15);
    }

    public String getCancelMaterial() {
        return shopConfig.getString("confirm-gui.cancel.material", "RED_STAINED_GLASS_PANE");
    }

    public String getCancelName() {
        return shopConfig.getString("confirm-gui.cancel.name", "&c&lCancel");
    }

    public List<String> getCancelLore() {
        return shopConfig.getStringList("confirm-gui.cancel.lore");
    }

    public int getItemDisplaySlot() {
        return shopConfig.getInt("confirm-gui.item-display-slot", 13);
    }

    public Map<String, ShopItem> getShopItems() {
        return Collections.unmodifiableMap(shopItems);
    }

    public ShopItem getShopItem(String id) {
        return shopItems.get(id);
    }

    public ShopItem getShopItemBySlot(int slot) {
        for (ShopItem item : shopItems.values()) {
            if (item.getSlot() == slot) {
                return item;
            }
        }
        return null;
    }

    public void addShopItem(String id, ShopItem item) {
        shopItems.put(id, item);
        save();
    }

    public void removeShopItem(String id) {
        shopItems.remove(id);
        save();
    }

    public void updateShopItem(String id, ShopItem item) {
        shopItems.put(id, item);
        save();
    }

    public void setShopRows(int rows) {
        shopConfig.set("shop.rows", rows);
        try {
            shopConfig.save(shopFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save shop.yml: " + e.getMessage());
        }
    }

    public void setShopTitle(String title) {
        shopConfig.set("shop.title", title);
        try {
            shopConfig.save(shopFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save shop.yml: " + e.getMessage());
        }
    }

    public ItemStack createShopItemStack(ShopItem item) {
        return createShopItemStack(item, null);
    }

    public ItemStack createShopItemStack(ShopItem item, Player player) {
        ItemStack itemStack;
        
        if (item.getSerializedItem() != null && !item.getSerializedItem().isEmpty()) {
            ItemStack deserialized = deserializeItemStack(item.getSerializedItem());
            if (deserialized != null) {
                itemStack = deserialized.clone();
            } else {
                itemStack = createBasicItemStack(item);
            }
        } else {
            itemStack = createBasicItemStack(item);
        }
        
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtils.colorize(item.getDisplayName()));
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Cost: &e" + item.getCost() + " Shards");
            lore.add("");
            
            List<String> customLore = item.getLore();
            if (customLore != null && !customLore.isEmpty()) {
                for (String line : customLore) {
                    lore.add(line);
                }
                lore.add("");
            }
            
            if (player != null) {
                long balance = plugin.getDatabaseManager().getBalance(player);
                if (balance >= item.getCost()) {
                    lore.add("&eClick to buy");
                } else {
                    lore.add("&cInsufficient Shards");
                }
            } else {
                lore.add("&eClick to buy");
            }
            
            meta.setLore(ColorUtils.colorize(lore));
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    private ItemStack createBasicItemStack(ShopItem item) {
        Material material;
        try {
            material = Material.valueOf(item.getMaterial().toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.STONE;
        }
        return new ItemStack(material, item.getAmount());
    }

    public ItemStack createEditorItemStack(ShopItem item) {
        ItemStack itemStack = createBasicItemStack(item);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtils.colorize(item.getDisplayName()));
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Cost: &e" + item.getCost() + " Shards");
            lore.add("&7Slot: &f" + item.getSlot());
            lore.add("");
            
            List<String> customLore = item.getLore();
            if (customLore != null && !customLore.isEmpty()) {
                lore.add("&7Custom Lore:");
                for (String line : customLore) {
                    lore.add("  " + line);
                }
            }
            lore.add("");
            lore.add("&eClick to edit");
            
            meta.setLore(ColorUtils.colorize(lore));
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public static String serializeItemStack(ItemStack item) {
        if (item == null) return null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public static ItemStack deserializeItemStack(String data) {
        if (data == null || data.isEmpty()) return null;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    public static class ShopItem {
        private String id;
        private int slot;
        private String material;
        private int amount;
        private String displayName;
        private List<String> lore;
        private int cost;
        private List<String> buyCommands;
        private String requiredPermission;
        private List<String> giveItems;
        private String serializedItem;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public int getSlot() { return slot; }
        public void setSlot(int slot) { this.slot = slot; }
        public String getMaterial() { return material; }
        public void setMaterial(String material) { this.material = material; }
        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public List<String> getLore() { return lore != null ? lore : new ArrayList<>(); }
        public void setLore(List<String> lore) { this.lore = lore; }
        public int getCost() { return cost; }
        public void setCost(int cost) { this.cost = cost; }
        public List<String> getBuyCommands() { return buyCommands != null ? buyCommands : new ArrayList<>(); }
        public void setBuyCommands(List<String> buyCommands) { this.buyCommands = buyCommands; }
        public String getRequiredPermission() { return requiredPermission; }
        public void setRequiredPermission(String requiredPermission) { this.requiredPermission = requiredPermission; }
        public List<String> getGiveItems() { return giveItems != null ? giveItems : new ArrayList<>(); }
        public void setGiveItems(List<String> giveItems) { this.giveItems = giveItems; }
        public String getSerializedItem() { return serializedItem; }
        public void setSerializedItem(String serializedItem) { this.serializedItem = serializedItem; }
        
        public boolean hasSerializedItem() {
            return serializedItem != null && !serializedItem.isEmpty();
        }
    }
}
