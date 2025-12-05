package com.oakshards.utils;

import com.oakshards.OakShards;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.Map;

public class MessageUtils {

    public static String replacePlaceholders(String message, Map<String, String> placeholders) {
        if (message == null) return "";
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        String prefix = OakShards.getInstance().getMessagesManager().getPrefix();
        message = message.replace("%prefix%", prefix);
        message = message.replace("{prefix}", prefix);
        
        return ColorUtils.colorize(message);
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(ColorUtils.colorize(message));
    }

    public static void sendMessage(Player player, String message, Map<String, String> placeholders) {
        player.sendMessage(replacePlaceholders(message, placeholders));
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
            TextComponent.fromLegacyText(ColorUtils.colorize(message)));
    }

    public static void sendActionBar(Player player, String message, Map<String, String> placeholders) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
            TextComponent.fromLegacyText(replacePlaceholders(message, placeholders)));
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(
            ColorUtils.colorize(title),
            ColorUtils.colorize(subtitle),
            fadeIn, stay, fadeOut
        );
    }

    public static void sendTitle(Player player, String title, String subtitle, 
                                  int fadeIn, int stay, int fadeOut, Map<String, String> placeholders) {
        player.sendTitle(
            replacePlaceholders(title, placeholders),
            replacePlaceholders(subtitle, placeholders),
            fadeIn, stay, fadeOut
        );
    }
}
