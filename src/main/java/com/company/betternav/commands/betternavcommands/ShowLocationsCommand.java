package com.company.betternav.commands.betternavcommands;

import com.company.betternav.commands.BetterNavCommand;
import com.company.betternav.navigation.LocationWorld;
import com.company.betternav.util.FileHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Map;

public class ShowLocationsCommand extends BetterNavCommand
{

    private final FileHandler fileHandler;
    private final YamlConfiguration config;

    public ShowLocationsCommand(FileHandler fileHandler, YamlConfiguration config)
    {
        this.fileHandler = fileHandler;
        this.config = config;
    }

    @Override
    public boolean execute(Player player, Command cmd, String s, String[] args, Map<String,String> messages){
        String primaryColor = messages.getOrDefault("primary_color", "§d");
        String secondaryColor = messages.getOrDefault("secondary_color", "§2");

        String message = primaryColor+messages.getOrDefault("saved_locations", "saved locations: ");
        player.sendMessage(message);

        String id = player.getUniqueId().toString();
        File parentFolder = new File(fileHandler.getPath());
        for (File worldFile : parentFolder.listFiles()) {
            if (!worldFile.isDirectory()){
                continue;
            }
            String readPath = worldFile.getPath()+File.separator;
            boolean privateWayPoints = config.getBoolean("privateWayPoints");
            if(privateWayPoints)
            {
                readPath = readPath+id+File.separator;
            }
            else
            {
                //create shared directory
                readPath = readPath+File.separator+"shared";
            }

            File folder = new File(readPath);
            File[] listOfFiles = folder.listFiles();

            if(listOfFiles==null||listOfFiles.length==0){
                continue;
            }
            player.sendMessage(worldFile.getName() + ":");

            for (File file : listOfFiles){
                if (file.isFile()) {
                    String[] fileName = file.getName().split(".json");
                    String locationName = fileName[0];
                    LocationWorld coordinates = fileHandler.readFile(locationName,player);
                    String locationInList = secondaryColor + messages.getOrDefault("locationindex", " - ") + locationName + " - " + ChatColor.WHITE + "(" + coordinates.getX() + ", " + coordinates.getY() + ", " + coordinates.getZ() + ")";
                    player.sendMessage(locationInList);
                }
            }
        }

        return true;
    }
}
