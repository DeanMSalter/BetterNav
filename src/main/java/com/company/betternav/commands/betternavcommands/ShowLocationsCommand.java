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
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Team team = board.registerNewTeam("teamname");
//Adding players
        team.addPlayer(player);

//Removing players
        team.removePlayer(player);

//Adding prefixes (shows up in player list before the player's name, supports ChatColors)
        team.setPrefix("prefix");

//Adding suffixes (shows up in player list after the player's name, supports ChatColors)
        team.setSuffix("suffix");

//Setting the display name
        team.setDisplayName("display name");

//Making invisible players on the same team have a transparent body
        team.setCanSeeFriendlyInvisibles(true);

//Making it so players can't hurt others on the same team
        team.setAllowFriendlyFire(false);

        Objective objective = board.registerNewObjective("test", "dummy", "");
//Setting where to display the scoreboard/objective (either SIDEBAR, PLAYER_LIST or BELOW_NAME)
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

//Setting the display name of the scoreboard/objective
        objective.setDisplayName("Locations");


//        Score score = objective.getScore(player.getName());
//        score.setScore(42); //Integer only!



        String primaryColor = messages.getOrDefault("primary_color", "§d");
        String secondaryColor = messages.getOrDefault("secondary_color", "§2");

        String message = primaryColor+messages.getOrDefault("saved_locations", "saved locations: ");
        player.sendMessage(message);

        String id = player.getUniqueId().toString();
        String world = player.getWorld().getName();

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
                    if (file.isFile())
                    {
                        // get full filename of file
                        String[] fileName = file.getName().split(".json");

                        // location name will be first part
                        String locationName = fileName[0];
                        //System.out.println(file.getName());
                        LocationWorld coordinates = fileHandler.readFile(locationName,player);
                        String locationInList = secondaryColor + messages.getOrDefault("locationindex", " - ")+ locationName;

                        //send message to the player
                        if (player.getWorld().getName().equals(coordinates.getWorld())){
                            locationInList = primaryColor + messages.getOrDefault("locationindex", " - ")+ locationName + " - " + "(" + Math.round(coordinates.getX()) + ", " + Math.round(coordinates.getY()) + ", " + Math.round(coordinates.getZ());
                            Location location = new Location( Bukkit.getWorld(coordinates.getWorld()), coordinates.getX(), coordinates.getY(), coordinates.getZ());

                            Double neededYaw = getYaw(location, player.getLocation());
                            Double degrees = normalAbsoluteAngleDegrees(Math.toDegrees(neededYaw));
                            Double playerYaw = Double.valueOf(player.getLocation().getYaw() + 180);
                            player.sendMessage(locationName);
                            player.sendMessage("player yaw " + playerYaw);
                            player.sendMessage("yaw" + degrees + " " + getCardinalDirection(degrees));


                            Vector direction = player.getLocation().getDirection();
                            Vector towardsEntity = location.subtract(player.getLocation()).toVector().normalize();
//                            double angle = Math.acos(direction.dot(towardsEntity));
//                            angle = Math.toDegrees(angle);


                            String arrow = calculateDirection(playerYaw, degrees);

                            double facing = direction.distance(towardsEntity);
//                            player.sendMessage(degrees + " " + facing + " - " +locationName);
                            if (facing < 0.2) {
                                arrow = "§2" + arrow;
                            } else if(facing > 0.2 && facing < 1.5) {
                                arrow = "§6" + arrow;
                            } else {
                                arrow = "§4" + arrow;
                            }


                            Score score = objective.getScore(arrow + ChatColor.GREEN + locationName);
                            score.setScore((int) player.getLocation().distance(location));
                        }
                        player.sendMessage(locationInList);
                    }
                }
        }
        player.setScoreboard(board);

        return true;
    }
    //TODO: fix this
    private static String calculateDirection(double playersYaw, double locationYaw){
        int cw = 0;
        double cwPlayersYaw = playersYaw;
        while(Math.abs(cwPlayersYaw - locationYaw) > 10){
            if(cwPlayersYaw >= 360) {
                cwPlayersYaw = 0;
            }
            cwPlayersYaw += 10;
            cw++;
            if (cw > 36){
                break;
            }
        }
        int acw = 0;
        double acwPlayersYaw = playersYaw;
        while(Math.abs(acwPlayersYaw - locationYaw) > 10){
            if(acwPlayersYaw <= 0) {
                acwPlayersYaw = 360;
            }
            acwPlayersYaw -= 10;
            acw++;
            if (acw > 36){
                break;
            }
        }
        if (Math.max(acw, cw) < 3){
            return "↑";
        } else if (Math.min(acw, cw) > 15) {
            return "↓";
        }else if (acw < cw){
           if (acw <= 4){
               return "⬉";
           }else if (acw <= 12){
               return "←";
           }else {
               return "⬋";
           }
        } else {
            if (cw <= 4) {
                return "⬈";
            }else if (cw <= 12){
                return "→";
            }else {
                return "⬊";
            }
        }

        //        double antiClockwise = Math.abs(locationYaw - playersYaw);
//        double clockwise =  Math.abs(((360 + locationYaw) - playersYaw)) ;
//        double notSure = (playersYaw-locationYaw+360)%360;
//        if (Math.abs(playersYaw - locationYaw) < 30) {
//            return "↑";
//        } else if (Math.abs(180 - notSure) < 30) {
//            return "↓";
//        } else if (notSure>180){
//            return "→";
//        } else {
//            return "←";
//        }
    }


    public static String getCardinalDirection(Double rotation) {
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return "↑";
        }
//        else if (22.5 <= rotation && rotation < 67.5) {
//            return "⬈";
//        }
        else if (67.5 <= rotation && rotation < 112.5) {
            return "→";
        }
//        else if (112.5 <= rotation && rotation < 157.5) {
//            return "⬊";
//        }
        else if (157.5 <= rotation && rotation < 202.5) {
            return "↓";
        }
//        else if (202.5 <= rotation && rotation < 247.5) {
//            return "⬋";
//        }
        else if (247.5 <= rotation && rotation < 292.5) {
            return "←";
        }
//        else if (292.5 <= rotation && rotation < 337.5) {
//            return "⬉";
//        }
        else if (337.5 <= rotation && rotation < 360.0) {
            return "↑";
        } else {
            return null;
        }
    }
    public double getYaw(Location locA, Location locB) {
        Vector dir  = locB.subtract(locA).toVector();
        dir.setY(0);
        dir = dir.normalize();
        return dir.getX() > 0 ? -Math.acos(dir.getZ()) : Math.acos(dir.getZ());
    }
    public static double normalAbsoluteAngleDegrees(double angle) {
        return (angle %= 360) >= 0 ? angle : (angle + 360);
    }

}

//← → ↑ ↓ ⬉ ⬈ ⬊ ⬋
