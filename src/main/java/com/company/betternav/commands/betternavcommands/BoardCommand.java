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

public class BoardCommand extends BetterNavCommand
{

    private final YamlConfiguration config;
    private final PlayerGoals playerGoals;

    private final BetterNav betterNav;

    public BoardCommand(YamlConfiguration config, PlayerGoals playerGoals, BetterNav betterNav){
        this.config = config;
        this.playerGoals = playerGoals;
        this.betterNav = betterNav;
    }

    @Override
    public boolean execute(Player player, Command cmd, String s, String[] args, Map<String,String> messages){
        Boolean boardStatus = this.betterNav.getBoardStatus(player.getUniqueId());
        boolean newBoardStatus = this.betterNav.setBoardStatus(player.getUniqueId(), !boardStatus);

        if (newBoardStatus){
            player.sendMessage("Location board turned on!");
        } else {
            player.sendMessage("Location board turned off!");
        }

        return true;
    }
}
