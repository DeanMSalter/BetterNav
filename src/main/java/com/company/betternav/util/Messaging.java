package com.company.betternav.util;

import com.company.betternav.BetterNav;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class Messaging {

    public static void sendMessage(Player player, ChatColor chatColor, String message){
        player.sendMessage(chatColor + message);
    }
    public static void sendPrimaryColourMessage(Player player, String message){
        String primaryColor = BetterNav.getMessages().getOrDefault("primary_color", "§d");
        player.sendMessage(primaryColor + message);
    }
}
