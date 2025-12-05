package com.oakshards.config;

import com.oakshards.OakShards;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessagesManager {

    private final OakShards plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public MessagesManager(OakShards plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reload() {
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getPrefix() {
        return messages.getString("prefix", "&8[&aOakShards&8] &r");
    }

    public String getMessage(String path) {
        return messages.getString(path, "&cMessage not found: " + path);
    }

    public String getNoPermission() {
        return getMessage("general.no-permission");
    }

    public String getPlayerNotFound() {
        return getMessage("general.player-not-found");
    }

    public String getInvalidNumber() {
        return getMessage("general.invalid-number");
    }

    public String getPlayerOnly() {
        return getMessage("general.player-only");
    }

    public String getReloaded() {
        return getMessage("general.reloaded");
    }

    public String getBalanceSelf() {
        return getMessage("balance.self");
    }

    public String getBalanceOther() {
        return getMessage("balance.other");
    }

    public String getAdminGive() {
        return getMessage("admin.give");
    }

    public String getAdminTake() {
        return getMessage("admin.take");
    }

    public String getAdminSet() {
        return getMessage("admin.set");
    }

    public String getAdminNotEnoughShards() {
        return getMessage("admin.not-enough-shards");
    }

    public String getAFKAreaCreated() {
        return getMessage("afk-area.created");
    }

    public String getAFKAreaRemoved() {
        return getMessage("afk-area.removed");
    }

    public String getAFKAreaNotFound() {
        return getMessage("afk-area.not-found");
    }

    public String getAFKAreaAlreadyExists() {
        return getMessage("afk-area.already-exists");
    }

    public String getPos1Set() {
        return getMessage("afk-area.pos1-set");
    }

    public String getPos2Set() {
        return getMessage("afk-area.pos2-set");
    }

    public String getNeedPositions() {
        return getMessage("afk-area.need-positions");
    }

    public String getAFKListHeader() {
        return getMessage("afk-area.list-header");
    }

    public String getAFKListEntry() {
        return getMessage("afk-area.list-entry");
    }

    public String getAFKListEmpty() {
        return getMessage("afk-area.list-empty");
    }

    public String getShopOpen() {
        return getMessage("shop.open");
    }

    public String getShopNotEnoughShards() {
        return getMessage("shop.not-enough-shards");
    }

    public String getShopPurchaseSuccess() {
        return getMessage("shop.purchase-success");
    }

    public String getShopNoPermission() {
        return getMessage("shop.no-permission-item");
    }

    public String getAFKInfoInArea() {
        return getMessage("afk-info.in-area");
    }

    public String getAFKInfoNotInArea() {
        return getMessage("afk-info.not-in-area");
    }

    public String getAFKInfoNextShard() {
        return getMessage("afk-info.next-shard");
    }

    public String getAFKInfoEarningRate() {
        return getMessage("afk-info.earning-rate");
    }

    public String getShardEarned() {
        return getMessage("shard-earned");
    }

    public String getTopHeader() {
        return getMessage("top.header");
    }

    public String getTopEntry() {
        return getMessage("top.entry");
    }

    public String getTopFooter() {
        return getMessage("top.footer");
    }

    public String getTopEmpty() {
        return getMessage("top.empty");
    }

    public String getTopPageInfo() {
        return getMessage("top.page-info");
    }

    public String getEditorTitle() {
        return getMessage("editor.title");
    }

    public String getEditorItemCreated() {
        return getMessage("editor.item-created");
    }

    public String getEditorItemRemoved() {
        return getMessage("editor.item-removed");
    }

    public String getEditorItemUpdated() {
        return getMessage("editor.item-updated");
    }

    public String getEditorEnterValue() {
        return getMessage("editor.enter-value");
    }

    public String getEditorCancelled() {
        return getMessage("editor.cancelled");
    }
}
