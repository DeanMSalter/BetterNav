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

import java.util.Map;
import java.util.UUID;

public class FriendsCommand extends BetterNavCommand
{

    private final YamlConfiguration config;
    private final PlayerGoals playerGoals;

    private final BetterNav betterNav;

    public FriendsCommand(YamlConfiguration config, PlayerGoals playerGoals, BetterNav betterNav) {
        this.config = config;
        this.playerGoals = playerGoals;
        this.betterNav = betterNav;
    }

    @Override
    public boolean execute(Player player, Command cmd, String s, String[] args, Map<String,String> messages) {
        UUID playerID = player.getUniqueId();
        Friend friendRecord = BetterNav.getFriendRecord(playerID);

        Map<UUID, String> friends = friendRecord.getFriends();
        if (friends.isEmpty()) {
            Messaging.sendPrimaryColourMessage(player, "You have no friends!");
            return true;
        }
        String message = "";
        if (friends.size() > 1){
            for (String value : friends.values()) {
                message += value + ", ";
            }
        } else {
            message = (String) friends.values().toArray()[0];
        }

        Messaging.sendPrimaryColourMessage(player, message);
        return true;
    }
}
