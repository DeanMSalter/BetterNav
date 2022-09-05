package com.company.betternav.commands.betternavcommands;

import com.company.betternav.BetterNav;
import com.company.betternav.commands.BetterNavCommand;
import com.company.betternav.navigation.Navigation;
import com.company.betternav.navigation.PlayerGoal;
import com.company.betternav.navigation.PlayerGoals;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class DenyCommand extends BetterNavCommand
{

    private final YamlConfiguration config;
    private final PlayerGoals playerGoals;

    private final BetterNav betterNav;

    public DenyCommand(YamlConfiguration config, PlayerGoals playerGoals, BetterNav betterNav){
        this.config = config;
        this.playerGoals = playerGoals;
        this.betterNav = betterNav;
    }

    @Override
    public boolean execute(Player player, Command cmd, String s, String[] args, Map<String,String> messages){
        UUID uuid = betterNav.getNavigationByAccepter(player.getUniqueId());
        if (uuid == null){
            String primaryColor = messages.getOrDefault("primary_color", "§d");
            String message = primaryColor + "Could not find any requests to accept";
            player.sendMessage(message);
            return true;
        }
        betterNav.declineAcceptRequest(player.getUniqueId());
        return true;
    }
}
