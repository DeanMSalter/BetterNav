package com.company.betternav.commands.betternavcommands;

import com.company.betternav.BetterNav;
import com.company.betternav.commands.BetterNavCommand;
import com.company.betternav.navigation.Navigation;
import com.company.betternav.navigation.PlayerGoal;
import com.company.betternav.navigation.PlayerGoals;
import com.company.betternav.util.Friend;
import com.company.betternav.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class NavPlayerCommand extends BetterNavCommand
{

    private final YamlConfiguration config;
    private final PlayerGoals playerGoals;

    private final BetterNav betterNav;

    public NavPlayerCommand(YamlConfiguration config, PlayerGoals playerGoals, BetterNav betterNav) {
        this.config = config;
        this.playerGoals = playerGoals;
        this.betterNav = betterNav;
    }

    @Override
    public boolean execute(Player player, Command cmd, String s, String[] args, Map<String,String> messages) {
        // if location provided
        if (args.length == 1){
            try
            {
                // get the location needed
                String playerName = args[0];

                Player navto = Bukkit.getPlayer(playerName);

                // if location is null
                if(navto==null){
                    String primaryColor = messages.getOrDefault("primary_color", "§d");
                    String message = primaryColor + messages.getOrDefault("player_not_found", "Could not find player");
                    player.sendMessage(message);
                    return true;
                }

                //if player casts navplayer command to himself
                if(player.getName().equals(navto.getName())){
                    String primaryColor = messages.getOrDefault("primary_color", "§d");
                    String message = primaryColor + messages.getOrDefault("nav_to_yourself", "Cannot cast navigation to yourself");
                    player.sendMessage(message);
                    return true;
                }

                if (this.betterNav.getNavigationByRequester(player.getUniqueId()) != null){
                    String primaryColor = messages.getOrDefault("primary_color", "§d");
                    String message = primaryColor + "You have already requested a navigation, use /stopnav to clear your request";
                    player.sendMessage(message);
                    return true;
                }

                if (this.betterNav.getNavigationByAccepter(navto.getUniqueId()) != null){
                    String primaryColor = messages.getOrDefault("primary_color", "§d");
                    String message = primaryColor + "That player already has a navigation request pending, they must /accept or /deny";
                    player.sendMessage(message);
                    return true;
                }


                this.betterNav.addRequest(player.getUniqueId(), navto.getUniqueId());

                Messaging.sendPrimaryColourMessage(navto, player.getName() + " has requested to navigate to you /accept to accept");
                Messaging.sendPrimaryColourMessage(player,  "waiting for " + navto.getName() + " to accept your navigation request.");
            }
            catch (IllegalArgumentException e){
                String primaryColor = messages.getOrDefault("primary_color", "§d");
                player.sendMessage( primaryColor +messages.getOrDefault("error", "/bn to get information about how to use Betternav commands"));
            }
        }
        return true;
    }
}
