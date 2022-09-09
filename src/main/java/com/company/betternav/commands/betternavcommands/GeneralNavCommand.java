package com.company.betternav.commands.betternavcommands;

import com.company.betternav.BetterNav;
import com.company.betternav.commands.BetterNavCommand;
import com.company.betternav.navigation.Goal;
import com.company.betternav.navigation.LocationWorld;
import com.company.betternav.navigation.Navigation;
import com.company.betternav.navigation.PlayerGoals;
import com.company.betternav.util.FileHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;

public class GeneralNavCommand extends BetterNavCommand {

    private final YamlConfiguration config;
    private final PlayerGoals playerGoals;
    private final FileHandler fileHandler;

    private final BetterNav betterNav;

    public GeneralNavCommand(YamlConfiguration config, PlayerGoals playerGoals, FileHandler fileHandler, BetterNav betterNav) {
        this.config = config;
        this.playerGoals = playerGoals;
        this.fileHandler = fileHandler;
        this.betterNav = betterNav;
    }

    @Override
    public boolean execute(Player player, Command cmd, String s, String[] args, Map<String, String> messages) {
        try {
            if (args.length < 1) {
                player.sendMessage("No navigation goal supplied");
                return true;
            }

            String location = args[0];

            // read coordinates out of file
            LocationWorld coordinates = fileHandler.readFile(location,player);

            // error handling when location is wrong
            if(coordinates!=null){
                new NavCommand(fileHandler, playerGoals, config).execute(player, cmd, s, args, messages);
                return true;
            }

            if (args.length == 3){
                new LocationNavCommand(config, playerGoals, betterNav).execute(player, cmd, s, args, messages);
                return true;
            }

            if (args.length == 1){
                new NavPlayerCommand(config,playerGoals, betterNav).execute(player,cmd,s,args,messages);
                return true;
            }

            player.sendMessage("Could not find something to navigate to. Please try again.");
        } catch (IllegalArgumentException e) {
            player.sendMessage(messages.getOrDefault("error", "/bn to get information about how to use Betternav commands"));
        }
        return true;
    }
}
