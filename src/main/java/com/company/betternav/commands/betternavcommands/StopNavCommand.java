package com.company.betternav.commands.betternavcommands;

import com.company.betternav.BetterNav;
import com.company.betternav.commands.BetterNavCommand;
import com.company.betternav.events.NavBossBar;
import com.company.betternav.navigation.PlayerGoals;
import com.company.betternav.util.Friend;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StopNavCommand extends BetterNavCommand
{
    private final PlayerGoals playerGoals;
    // hashmap to keep track of players with their bossbar
    private final HashMap<UUID, NavBossBar> bblist;
    private final BetterNav betterNav;

    public StopNavCommand(PlayerGoals playerGoals, HashMap<UUID, NavBossBar> bblist, BetterNav betterNav) {
        this.playerGoals = playerGoals;
        this.bblist = bblist;
        this.betterNav = betterNav;
    }

    @Override
    public boolean execute(Player player, Command cmd, String s, String[] args, Map<String,String> messages)
    {
        try{
            UUID playerID = player.getUniqueId();
            // delete player at navigating people
            this.playerGoals.removePlayerGoal(playerID);

            // delete the bossbar
            NavBossBar delbb = bblist.remove(playerID);
            delbb.delete(player);

            // remove the bar of the list
            bblist.remove(playerID);

            betterNav.removeRequest(playerID);

            Friend friendRecord = BetterNav.getFriendRecord(playerID);
            friendRecord.removeAllFriendRequests();

            Map<UUID, Friend> friendRecords = BetterNav.getAllFriendRecords();
            for (UUID uuid :friendRecords.keySet()) {
                if (uuid == playerID){
                    continue;
                }
                Friend potentialFriendRecord = friendRecords.get(uuid);
                if (potentialFriendRecord.removeFriendRequest(playerID)){
                    break;
                }
            }

            // send ending navigation message
            String primaryColor = messages.getOrDefault("primary_color", "§d");
            String message = primaryColor + messages.getOrDefault("ending_navigation", ChatColor.LIGHT_PURPLE + "ending navigation");
            player.sendMessage(message);

        }
        catch (Exception e)
        {
            String primaryColor = messages.getOrDefault("primary_color", "§d");
            String message = primaryColor + messages.getOrDefault("cannot_end_navigation", "Cannot end navigation");
            player.sendMessage(message);
        }

        return true;
    }
}
