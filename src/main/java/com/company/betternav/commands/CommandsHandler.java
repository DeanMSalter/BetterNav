package com.company.betternav.commands;

import com.company.betternav.BetterNav;
import com.company.betternav.commands.betternavcommands.*;
import com.company.betternav.events.NavBossBar;
import com.company.betternav.navigation.PlayerGoals;
import com.company.betternav.util.FileHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;


/*
Command Handler for BetterNavigating Plugin
 */
public class CommandsHandler implements CommandExecutor
{

    private final Map<String, BetterNavCommand> commandMap;
    private final Map<String,String> messages;

    /**
     *  Constructor for command handler
     *
     * @param playerGoals class
     * @param betterNav, to get the path extracted
     *
     */
    public CommandsHandler(YamlConfiguration config, PlayerGoals playerGoals, BetterNav betterNav, HashMap<UUID,Boolean> actionbarplayers, HashMap<UUID, NavBossBar> bblist, Map<String,String> messages)
    {
        this.messages = messages;
        FileHandler fileHandler = new FileHandler(betterNav, config,messages);

        this.commandMap = new HashMap<String, BetterNavCommand>()
        {
            {
            put("bn",               new BnCommand());
            put("getlocation",      new GetLocationCommand(actionbarplayers));
            put("showlocations",    new ShowLocationsCommand(fileHandler, config));
            put("savelocation",     new SaveLocationCommand(fileHandler));
            put("del",              new DelCommand(fileHandler));
            put("showcoordinates",  new ShowCoordinatesCommand(fileHandler,config));
            put("nav",              new NavCommand(fileHandler, playerGoals, config));
            put("navplayer",        new NavPlayerCommand(config, playerGoals, betterNav));
            put("navlocation",      new LocationNavCommand(config, playerGoals, betterNav));
            put("stopnav",          new StopNavCommand(playerGoals, bblist, betterNav));
            put("accept",           new AcceptCommand(config, playerGoals, betterNav));
            put("deny",             new DenyCommand(config, playerGoals, betterNav));
            put("board",            new BoardCommand(config, playerGoals, betterNav));
            put("navgeneral",       new GeneralNavCommand(config, playerGoals, fileHandler, betterNav));

            }
        };
    }

    /**
     *
     * @param sender sender of the command
     * @param cmd commands
     * @param s message
     * @param args arguments
     * @return
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args)
    {
        // check if a player was the sender of the command
        if (!(sender instanceof Player))
        {
            sender.sendMessage( messages.getOrDefault("only_players", "only players can use that command"));
            return true;
        }

        // Return false if the command is not found
        String command = cmd.getName().toLowerCase();
        if ( ! commandMap.containsKey( command ))
            return false;

        // Use the command object for cmd execution
        Player player = (Player) sender;
        return commandMap.get( command ).execute( player, cmd, s, args ,messages);

    }
}