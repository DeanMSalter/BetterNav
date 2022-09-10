package com.company.betternav.util;

import com.company.betternav.navigation.Board;
import com.company.betternav.navigation.Goal;
import com.company.betternav.navigation.LocationWorld;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static java.lang.String.valueOf;

public class FileHandler
{

    // local path, where the files will need to be stored
    private final String path;
    private final YamlConfiguration config;
    private final Map<String,String> messages;

    public FileHandler(JavaPlugin plugin, YamlConfiguration config, Map<String,String> messages){
        // File.separator to get correct separation, depending on OS
        this.path = plugin.getDataFolder().getAbsolutePath() + File.separator;
        this.config = config;
        this.messages = messages;
    }

    public String getPath(){
        return this.path;
    }

    /**
     * Makes a directory in the certain path if the directory doesn't exist
     */
    public void makeDirectory(String newPath){
        // Create missing folder at path
        File folder = new File(newPath);
        if (!folder.exists()) folder.mkdir();
    }


    /**
     * To write the file that consists of the coordinates
     * @param player player who did the command
     * @param playerGoal the name of the goal and Location location
     */

    public void writeLocationFile(Player player, Goal playerGoal){
        // initiate json parser Gson
        Gson json = new GsonBuilder().setPrettyPrinting().create();

        try
        {

            // if it does not exist: create missing folder Betternav
            makeDirectory(path);

            // read if private waypoints is enabled or not
            boolean privateWayPoints = config.getBoolean("privateWayPoints");

            // get the worldname where the player is active
            String world = player.getWorld().getName();

            // attach the world path to the original path
            String worldPath = path+File.separator+world;

            // create missing folder world
            makeDirectory(worldPath);

            // put the world path equal to the player path
            String PlayerPath = worldPath;


            // if private waypoints is enabled, attach player information
            if(privateWayPoints)
            {

                // get player uuid
                UUID uuid = player.getUniqueId();

                // get the uuid of the player
                String id = uuid.toString();

                // put the player path equal to the original path + uuid
                PlayerPath = worldPath+File.separator+id;

            }

            // use the shared directory
            else
            {
                // create/use the shared directory
                PlayerPath = PlayerPath+File.separator+"shared";
            }

            // create playerPath if it doesn't exist
            makeDirectory(PlayerPath);

            // get the maximum of waypoints in the configuration file
            int maximumWayPoints = config.getInt("maximumWaypoints");

            // create new file string in the directory with filename: name
            String filename = PlayerPath+File.separator+playerGoal.getName()+".json";

            // create the file
            File directory = new File(PlayerPath);

            // check for the length of files in the directory
            int fileCount = directory.list().length;

            // check if the number of files
            if(fileCount<=maximumWayPoints || playerGoal.getName().equals("death_location"))
            {

                // get x and z location (string)
                int X_Coordinate = playerGoal.getLocation().getBlockX();
                int Y_Coordinate = playerGoal.getLocation().getBlockY();
                int Z_Coordinate = playerGoal.getLocation().getBlockZ();

                // create string value of locations (to send message later on)
                String X = valueOf(X_Coordinate);
                String Y = valueOf(Y_Coordinate);
                String Z = valueOf(Z_Coordinate);

                //make a filewriter
                FileWriter myWriter = new FileWriter(filename);

                // make map of coordinates and name to define it in json
                LocationWorld coordinate = new LocationWorld(world,playerGoal.getName(),Integer.parseInt(X),Integer.parseInt(Y),Integer.parseInt(Z));

                // write to Json file
                json.toJson(coordinate,myWriter);

                // close writer
                myWriter.close();

                // send player verification message
                String primaryColor = messages.getOrDefault("primary_color", "§d");
                String secondaryColor = messages.getOrDefault("secondary_color", "§2");

                String locsaved = primaryColor + messages.getOrDefault("location_saved", "§c§l(!) §c Location <location> saved on:");

                // append data to location save command
                locsaved = locsaved + secondaryColor + " X:"+X+" Y: "+Y+" Z: "+Z;

                String message = locsaved.replace("<location>", playerGoal.getName());
                player.sendMessage(message);
            }

            else
            {
                // send player message if limit is reached
                String primaryColor = messages.getOrDefault("primary_color", "§d");
                String message = primaryColor + messages.getOrDefault("maximum_amount", "Maximum amount of <number> waypoints reached");
                message = message.replace("<number>",String.valueOf(maximumWayPoints));
                player.sendMessage(message);
            }


        }
        catch (IOException e)
        {
            // send player message if error occurred
            String primaryColor = messages.getOrDefault("primary_color", "§d");
            String message = primaryColor + messages.getOrDefault("error_saving", "An error occurred by writing a file for your coordinates");
            player.sendMessage(message);
        }
    }

    public void writeFriendFile(UUID playerUUID, Friend friend){
        // initiate json parser Gson
        Gson json = new GsonBuilder().setPrettyPrinting().create();

        try
        {

            // if it does not exist: create missing folder Betternav
            makeDirectory(path);

            // attach the world path to the original path
            String worldPath = path+File.separator+"friends";

            // create missing folder world
            makeDirectory(worldPath);

            // create new file string in the directory with filename: name
            String filename = worldPath+File.separator+playerUUID+".json";

            FileWriter myWriter = new FileWriter(filename);
            json.toJson(friend,myWriter);
            // close writer
            myWriter.close();
        }
        catch (IOException e)
        {
            // send player message if error occurred
            String primaryColor = messages.getOrDefault("primary_color", "§d");
            String message = primaryColor + messages.getOrDefault("error_saving", "An error occurred by writing a file for your coordinates");
            Bukkit.getPlayer(playerUUID).sendMessage(message);
        }
    }

    public Friend readFriendFile(UUID playerUUID){
        // start a new json parser Gson
        Gson gson = new Gson();
        File parentFolder = new File(this.getPath());
        String filePath = path+File.separator+"friends"+File.separator+playerUUID+".json";
        // create a string of the path of the file to be read

        // try to read the file (if exists)
        try (Reader reader = new FileReader(filePath))
        {

            // Convert JSON File to Java Object
            // return the class
            return gson.fromJson(reader, Friend.class);

        }
        catch (IOException e){

        }
        return null;
    }


    public void writeBoardFile(UUID playerUUID, Board board){
        // initiate json parser Gson
        Gson json = new GsonBuilder().setPrettyPrinting().create();

        try
        {

            // if it does not exist: create missing folder Betternav
            makeDirectory(path);

            // attach the world path to the original path
            String worldPath = path+File.separator+"boards";

            // create missing folder world
            makeDirectory(worldPath);

            // create new file string in the directory with filename: name
            String filename = worldPath+File.separator+playerUUID+".json";

            FileWriter myWriter = new FileWriter(filename);
            json.toJson(board,myWriter);
            // close writer
            myWriter.close();
        }
        catch (IOException e)
        {
            // send player message if error occurred
            String primaryColor = messages.getOrDefault("primary_color", "§d");
            String message = primaryColor + messages.getOrDefault("error_saving", "An error occurred by writing a file for your coordinates");
            Bukkit.getPlayer(playerUUID).sendMessage(message);
        }
    }

    public Board readBoardFile(UUID playerUUID){
        // start a new json parser Gson
        Gson gson = new Gson();
        File parentFolder = new File(this.getPath());
        String filePath = path+File.separator+"boards"+File.separator+playerUUID+".json";
        // create a string of the path of the file to be read

        // try to read the file (if exists)
        try (Reader reader = new FileReader(filePath))
        {

            // Convert JSON File to Java Object
            // return the class
            return gson.fromJson(reader, Board.class);

        }
        catch (IOException e){

        }
        return null;
    }

    /**
     *
     * To delete a waypoint
     *
     * @param location the location to be deleted
     * @param player the player who did execute the deletion
     * @return boolean if the file is gone
     */
    public boolean deleteFile(String location,Player player){

        // get the player id and world
        String id = player.getUniqueId().toString();
        String world = player.getWorld().getName();

        // check if the waypoints are private or not
        boolean privateWayPoints = config.getBoolean("privateWayPoints");


        // add the world name to the file path
        String readPath = path+File.separator+world+File.separator;


        // if private enabled, add uuid of player
        if(privateWayPoints)
        {
            readPath = readPath + id + File.separator;
        }

        else
        {
            //use shared directory
            readPath = readPath+"shared"+File.separator;
        }

        // create the directory
        makeDirectory(readPath);

        // create the full path to be read
        readPath = readPath + location+".json";

        // create new file object
        File file = new File(readPath);

        // if the file is deleted
        if(file.delete())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     *
     * Used to read a file
     *
     * @param location the location the player wants to have
     * @param player the player who did the action
     * @return object of class LocationWorld
     */

    public LocationWorld readFile(String location,Player player){
        // start a new json parser Gson
        Gson gson = new Gson();


        File parentFolder = new File(this.getPath());

        for (File worldFile : parentFolder.listFiles()) {
            // get the uuid of the player who did the command
            String uuid = player.getUniqueId().toString();

            // make up the world path
            String worldPath = worldFile.getPath()+File.separator;

            // get the config of privatewaypoins
            boolean privateWayPoints = config.getBoolean("privateWayPoints");

            // if it is enabled: PlayerPath will be needed the uuid of the player
            if(privateWayPoints)
            {
                // add the uuid of the player
                String playerPath = worldPath+uuid;

                // make up the world path out of the player path
                worldPath = playerPath;
            }

            else
            {
                //create shared directory
                worldPath = worldPath+File.separator+"shared";
            }

            // make a directory of the world path
            makeDirectory(worldPath);

            // create a string of the path of the file to be read
            String readPath = worldPath+File.separator+location+".json";

            // try to read the file (if exists)
            try (Reader reader = new FileReader(readPath))
            {

                // Convert JSON File to Java Object
                // return the class
                return gson.fromJson(reader, LocationWorld.class);

            }
            catch (IOException e){

            }
        }

        return null;
    }

    public List<LocationWorld> getLocationsInWorld(World world, Player player){
        List<LocationWorld> locations = new ArrayList<>();
        File parentFolder = new File(this.getPath());
        String id = player.getUniqueId().toString();

        for (File worldFile : parentFolder.listFiles()) {
            if (!worldFile.isDirectory()){
                continue;
            }
            if (!worldFile.getName().equals(world.getName())){
                continue;
            }
            String readPath = worldFile.getPath()+File.separator;
            boolean privateWayPoints = config.getBoolean("privateWayPoints");
            if(privateWayPoints){
                readPath = readPath+id+File.separator;
            }
            else{
                //create shared directory
                readPath = readPath+File.separator+"shared";
            }

            File folder = new File(readPath);
            File[] listOfFiles = folder.listFiles();

            if(listOfFiles==null||listOfFiles.length==0){
                continue;
            }
            for (File file : listOfFiles){
                if (file.isFile()) {
                    String[] fileName = file.getName().split(".json");
                    String locationName = fileName[0];
                    LocationWorld coordinates = this.readFile(locationName,player);
                    locations.add(coordinates);
                }
            }
        }

        return locations;
    }
}
