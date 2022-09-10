package com.company.betternav.commands.betternavcommands;

import com.company.betternav.BetterNav;
import com.company.betternav.commands.BetterNavCommand;
import com.company.betternav.navigation.PlayerGoals;
import com.company.betternav.util.Friend;
import com.company.betternav.util.Messaging;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class RemoveFriendCommand extends BetterNavCommand
{

    private final YamlConfiguration config;
    private final PlayerGoals playerGoals;

    private final BetterNav betterNav;

    public RemoveFriendCommand(YamlConfiguration config, PlayerGoals playerGoals, BetterNav betterNav) {
        this.config = config;
        this.playerGoals = playerGoals;
        this.betterNav = betterNav;
    }

    @Override
    public boolean execute(Player player, Command cmd, String s, String[] args, Map<String,String> messages) {
        if (args.length < 1) {
            return true;
        }

        UUID playerID = player.getUniqueId();
        Friend friendRecord = BetterNav.getFriendRecord(playerID);
        BetterNav.getFriendRecord(friendRecord.getFriendID(args[0])).removeFriend(playerID);
        friendRecord.removeFriend(args[0]);
        Messaging.sendPrimaryColourMessage(player, "you have unfriended" + args[0]);
        return true;
    }
}
