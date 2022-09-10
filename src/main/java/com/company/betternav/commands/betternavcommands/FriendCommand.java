package com.company.betternav.commands.betternavcommands;

import com.company.betternav.BetterNav;
import com.company.betternav.commands.BetterNavCommand;
import com.company.betternav.navigation.PlayerGoals;
import com.company.betternav.util.Friend;
import com.company.betternav.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FriendCommand extends BetterNavCommand
{

    private final YamlConfiguration config;
    private final PlayerGoals playerGoals;

    private final BetterNav betterNav;

    public FriendCommand(YamlConfiguration config, PlayerGoals playerGoals, BetterNav betterNav) {
        this.config = config;
        this.playerGoals = playerGoals;
        this.betterNav = betterNav;
    }

    @Override
    public boolean execute(Player player, Command cmd, String s, String[] args, Map<String,String> messages) {
        // if location provided
        if (args.length == 0) {
            return true;
        }
        try{
            UUID playerID = player.getUniqueId();
            Friend friendRecord = BetterNav.getFriendRecord(playerID);


            String newFriendName = args[0];
            Player newFriendPlayer = Bukkit.getPlayer(newFriendName);

            if(newFriendPlayer==null){
                Messaging.sendPrimaryColourMessage(player, "Could not find player to friend. Are they online? Have you entered their full name?");
                return true;
            }

            UUID newFriend = newFriendPlayer.getUniqueId();
            if(player.getName().equals(newFriendName)){
                Messaging.sendPrimaryColourMessage(player, "Can not friend yourself");
                return true;
            }


            if (friendRecord.isFriend(newFriend)){
                Messaging.sendPrimaryColourMessage(player, "You are already friends.");
                return true;
            }


            List<UUID> friendRequests = friendRecord.getFriendRequests();
            if (friendRequests != null && friendRequests.contains(newFriend)) {
                Messaging.sendPrimaryColourMessage(player, "You already have a pending friend request, use /stopnav to cancel");
                return true;
            }

            Friend newFriendRecord = BetterNav.getFriendRecord(newFriend);
            List<UUID> newFriendFriendRequests = newFriendRecord.getFriendRequests();

            if (newFriendFriendRequests != null && newFriendFriendRequests.contains(playerID)){
                newFriendRecord.addFriend(playerID);
                friendRecord.addFriend(newFriend);
                Messaging.sendPrimaryColourMessage(player, "You are now friends with" + newFriendName);
                Messaging.sendPrimaryColourMessage(newFriendPlayer, "You are now friends with" + player.getName());
                return true;
            }
            friendRecord.addFriendRequest(newFriend);

            Messaging.sendPrimaryColourMessage(newFriendPlayer, player.getName() + " has asked to be friends /friend " + player.getName() +  "to accept");
            Messaging.sendPrimaryColourMessage(player, "Waiting for " + newFriendName + " to accept your friend request.");
        }
        catch (IllegalArgumentException e){
            String primaryColor = messages.getOrDefault("primary_color", "§d");
            player.sendMessage( primaryColor +messages.getOrDefault("error", "/bn to get information about how to use Betternav commands"));
        }
        return true;
    }
}
