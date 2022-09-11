package com.company.betternav.commands.betternavcommands;

import com.company.betternav.BetterNav;
import com.company.betternav.commands.BetterNavCommand;
import com.company.betternav.navigation.Goal;
import com.company.betternav.navigation.Navigation;
import com.company.betternav.navigation.PlayerGoal;
import com.company.betternav.navigation.PlayerGoals;
import com.company.betternav.util.Friend;
import com.company.betternav.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ConvertCommand extends BetterNavCommand
{

    private final YamlConfiguration config;
    private final PlayerGoals playerGoals;

    private final BetterNav betterNav;

    public ConvertCommand(YamlConfiguration config, PlayerGoals playerGoals, BetterNav betterNav){
        this.config = config;
        this.playerGoals = playerGoals;
        this.betterNav = betterNav;
    }

    @Override
    public boolean execute(Player player, Command cmd, String s, String[] args, Map<String,String> messages){
        File file = new File("plugins/CoordsManager/PlayerData/" + player.getUniqueId()+ ".yml");
        if (file.length() == 0) {
            return true;
        }
        UUID playerID = player.getUniqueId();

        FileConfiguration playerData = YamlConfiguration.loadConfiguration(file);
        List<Map<String,Object>> locations = (List<Map<String, Object>>) playerData.getList("savedLocationList");
        for (Map<String, Object> loadedDestination : locations) {
            World world = Bukkit.getWorld(loadedDestination.get("world").toString());
            if (world == null) {
                player.sendMessage("could not save location, please let staff know " + loadedDestination.get("name").toString());
                continue;
            }

            Location location = new Location(world, Double.parseDouble(loadedDestination.get("x").toString()), Double.parseDouble(loadedDestination.get("y").toString()), Double.parseDouble(loadedDestination.get("z").toString()));

            Goal saveloc = new Goal(loadedDestination.get("name").toString(),location);
            BetterNav.getFileHandler().writeLocationFile(player,saveloc);

        }
        List<Map<String,Object>> friends = (List<Map<String, Object>>) playerData.getList("friendList");
        Friend friendRecord = BetterNav.getFriendRecord(playerID);

        for (Map<String, Object> friend : friends) {

            UUID newFriend = UUID.fromString(friend.get("uuid").toString());
            String newFriendName = friend.get("nickname").toString();
            if (friendRecord.isFriend(newFriend)){
                Messaging.sendPrimaryColourMessage(player, "You are already friends.");
                continue;
            }
            if(player.getName().equals(newFriendName)){
                Messaging.sendPrimaryColourMessage(player, "Can not friend yourself");
                continue;
            }

            Friend newFriendRecord = BetterNav.getFriendRecord(newFriend);
            newFriendRecord.addFriend(playerID, player.getName());
            friendRecord.addFriend(newFriend, newFriendName);
            Messaging.sendPrimaryColourMessage(player, "You are now friends with" + newFriendName);
        }
        this.betterNav.setBoardStatus(playerID, playerData.getBoolean("showBoard"), playerData.getInt("boardSize"));
        file.delete();
        return true;
    }
}
