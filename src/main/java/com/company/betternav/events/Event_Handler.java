package com.company.betternav.events;

import com.company.betternav.BetterNav;
import com.company.betternav.navigation.Board;
import com.company.betternav.navigation.Goal;
import com.company.betternav.bossbarcalculators.IBossBarCalculator;
import com.company.betternav.navigation.LocationWorld;
import com.company.betternav.navigation.Navigation;
import com.company.betternav.navigation.PlayerGoal;
import com.company.betternav.navigation.PlayerGoals;
import com.company.betternav.bossbarcalculators.AdvancedBossbarCalculator;
import com.company.betternav.bossbarcalculators.BasicCalculator;
import com.company.betternav.bossbarcalculators.IdeaBossBarCalculator;
import com.company.betternav.util.FileHandler;
import com.company.betternav.util.Friend;
import com.company.betternav.util.animation.LineAnimation;
import com.company.betternav.util.animation.SpiralAnimation;
import com.company.betternav.util.animation.location.PlayerLocation;
import com.company.betternav.util.animation.location.StaticLocation;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.valueOf;


public class Event_Handler implements Listener
{

    private final BetterNav plugin;
    private final PlayerGoals playerGoals;
    private HashMap<UUID, NavBossBar> bblist = new HashMap<>();
    private final IBossBarCalculator bossBarCalculator;
    private HashMap<UUID,Boolean> actionbarplayers = new HashMap<>();
    private final YamlConfiguration config;
    private final int distance_to_goal;
    private final Map<String,String> messages;
    private final boolean heightCheck;
    private final FileHandler fileHandler;

    ScoreboardManager manager = Bukkit.getScoreboardManager();

    // rounding function
    public double round(double value, int places)
    {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public Event_Handler(YamlConfiguration config, PlayerGoals playerGoals, BetterNav plugin, HashMap<UUID, Boolean> actionbarplayers, HashMap<UUID, NavBossBar> bblist, Map<String, String> messages, FileHandler fileHandler)
    {
        this.config = config;
        this.playerGoals = playerGoals;
        this.plugin = plugin;
        this.actionbarplayers = actionbarplayers;
        this.bblist = bblist;
        this.messages = messages;
        this.fileHandler = fileHandler;

        // get bb value out of config file
        int bbcalc = config.getInt("BossBar");

        if(bbcalc==1)
        {
            this.bossBarCalculator = new IdeaBossBarCalculator();
        }
        else if (bbcalc==2)
        {
            this.bossBarCalculator = new BasicCalculator();
        }
        else
        {
            this.bossBarCalculator = new AdvancedBossbarCalculator();
        }

        // get distance to goal value out of config file
        distance_to_goal = config.getInt("Distance");

        // get boolean heightcheck out of config file
        heightCheck = config.getBoolean("height_check");

    }

    // send welcome message when player joined
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        // check if welcomeMessage is enabled in config file
        boolean message = config.getBoolean("welcomeMessage");

        // get the player that joined
        Player player = event.getPlayer();
        BetterNav.addBoard(fileHandler.readBoardFile(player.getUniqueId()));
        // send him message
        if(message)
        {
            String primaryColor = messages.getOrDefault("primary_color", "§d");
            String welcomeMessage = primaryColor + messages.getOrDefault("welcome_message", ChatColor.LIGHT_PURPLE + "Betternav plugin enabled: /bn to get help");
            player.sendMessage(welcomeMessage);
        }

        // check if player had a navigation set
        Goal hadNav = playerGoals.getPlayerGoal(player.getUniqueId());
        if(hadNav!=null)
        {
            NavBossBar bb = new NavBossBar(plugin,config,messages);

            // put the bar on the list
            bblist.put(player.getUniqueId(),bb);

            double distance = Math.sqrt(Math.pow(((Math.round( player.getLocation().getBlockX()-hadNav.getLocation().getBlockX() ))),2)+(Math.pow(Math.round(player.getLocation().getBlockX()-hadNav.getLocation().getBlockX()),2)));
            distance = round(distance,2);

            // create a bar
            bb.createBar(hadNav.getName(),distance);

            // add player to the bar
            bb.addPlayer(player);
        }
    }


    private void buildScoreboard(Player player) {
        List<String> playersInPlayerWorld = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getLocation().getWorld().getName().equals(player.getWorld().getName())){
                playersInPlayerWorld.add(onlinePlayer.getName());
            }
        }


        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.getObjective(player.getUniqueId() + "locations");
        Board playerBoard = BetterNav.getBoards().get(player.getUniqueId());
        if (playerBoard == null){
            try{
                objective.unregister();
            }catch (NullPointerException e){

            }
            return;
        }

        if (!playerBoard.getStatus()) {
            try{
                player.setScoreboard(manager.getNewScoreboard());
                board.getEntries().forEach(board::resetScores);
                objective.unregister();
            }catch (NullPointerException e){

            }
            return;
        }

        if (objective == null){
            objective = board.registerNewObjective(player.getUniqueId() + "locations", "", "§b§lLocations");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
//            objective.unregister();
        }
        //        objective = board.registerNewObjective("locations", "", "§b§lLocations");
//        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<LocationWorld> locations = fileHandler.getLocationsInWorld(player.getWorld(), player);

        Friend friend = BetterNav.getFriendRecord(player.getUniqueId());
        friend.getFriends().keySet().forEach(uuid -> {
            Player potentialFriend = Bukkit.getPlayer(uuid);
            if (potentialFriend == null){
                return;
            }
            Location locationOfFriend = potentialFriend.getLocation();
            LocationWorld locationWorld = new LocationWorld(locationOfFriend.getWorld().getName(),  potentialFriend.getName(), (int) locationOfFriend.getX(), (int) locationOfFriend.getY(), (int) locationOfFriend.getZ());
            locations.add(0, locationWorld);
        });

        List<Double> distances = new ArrayList<>();
        for (LocationWorld location : locations) {
            double distance = player.getLocation().distance(new Location(Bukkit.getWorld(location.getWorld()), location.getX(), location.getY(), location.getZ()));
            location.setDistance(distance);
            distances.add(distance);
        }
        Collections.sort(distances);
        Collections.reverse(distances);

        Set<String> entries = board.getEntries();
        for (int i = 0; i < locations.size(); i++) {
            LocationWorld coordinates = locations.get(i);
            entries.forEach(s -> {
                if (s.contains(coordinates.getName())){
                    board.resetScores(s);
                }
            });
            int index = distances.indexOf(coordinates.getDistance());
            if (index >= playerBoard.getNumToShow()) {
                continue;
            }
            Location location = new Location(Bukkit.getWorld(coordinates.getWorld()), coordinates.getX(), coordinates.getY(), coordinates.getZ());
            double neededYaw = getYaw(location, player.getLocation());
            double degrees = normalAbsoluteAngleDegrees(Math.toDegrees(neededYaw));
            double playerYaw = player.getLocation().getYaw() + 180;
            String arrow = calculateDirection(playerYaw, degrees);

            String boardEntryName = arrow + ChatColor.WHITE + coordinates.getName() + ": §b" + (int) coordinates.getDistance();
            if (playersInPlayerWorld.contains(coordinates.getName())){
                boardEntryName = arrow + ChatColor.GOLD + coordinates.getName() + ": §b" + (int) coordinates.getDistance();
            }
            Score score = objective.getScore(boardEntryName);
            score.setScore(index);
        }

        player.setScoreboard(board);
    }
    //check if player has moved
    @EventHandler
    public void onPlayerWalk(PlayerMoveEvent event){

        Location loc = event.getTo();
        if(loc == null){
            return;
        }

        Player navPlayer = event.getPlayer();
        UUID uuid = navPlayer.getUniqueId();
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        buildScoreboard(player);
        // check for action bar
        if (actionbarplayers.containsKey(uuid)){
            // get boolean for player
            boolean actionbar = actionbarplayers.get(uuid);
            if(actionbar)
            {
                int X_Coordinate = navPlayer.getLocation().getBlockX();
                int Y_Coordinate = navPlayer.getLocation().getBlockY();
                int Z_Coordinate = navPlayer.getLocation().getBlockZ();

                String X = valueOf(X_Coordinate);
                String Y = valueOf(Y_Coordinate);
                String Z = valueOf(Z_Coordinate);

                String actionbarColor = messages.getOrDefault("actionbar_color", "§f");
                String message = actionbarColor+"X "+X+"          "+"Y "+Y+"          "+"Z "+Z;

                navPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            }
        }


        //check for bossbar
        Goal goal = this.playerGoals.getPlayerGoal( uuid );

        // Return if Player has no active goal
        if (goal == null)
            return;

        //get name of the goal
        String goalName = goal.getName();
        String ownWorld = navPlayer.getWorld().getName();
        String worldGoal = goal.getLocation().getWorld().getName();

        // if on different world
        if(ownWorld!=worldGoal){
            // send message to navigating person
            String primaryColor = messages.getOrDefault("primary_color", "§d");
            String message = primaryColor + messages.getOrDefault("different_world", "Target on different world");
            navPlayer.sendMessage(message);

            // delete player at navigating people
            this.playerGoals.removePlayerGoal( uuid );

            try
            {
                // delete the bossbar
                NavBossBar delbb = bblist.get(uuid);
                delbb.delete(navPlayer);

                // remove the bar of the list
                bblist.remove(navPlayer.getUniqueId());
            }
            catch(Exception e)
            {

            }
            return;
        }

        //get x,y and z value
        double x = goal.getLocation().getX();
        double y = goal.getLocation().getY();
        double z = goal.getLocation().getZ();

        //get current coordinates
        int x_nav = navPlayer.getLocation().getBlockX();
        int y_nav = navPlayer.getLocation().getBlockY();
        int z_nav = navPlayer.getLocation().getBlockZ();

        //calculate euclidean distance
        double distance = Math.sqrt(Math.pow(((Math.round( x-x_nav ))),2)+(Math.pow(Math.round(z-z_nav),2)));
        distance = round(distance,2);

        //create new bossbar
        NavBossBar bb = new NavBossBar(plugin,config,messages);

        //check if player exists
        if(bblist.containsKey(uuid)){
            // get bossbar of player
            NavBossBar navbb = bblist.get(uuid);

            // update the distance to the goal
            Location goalLocation = goal.getLocation();
            new LineAnimation(
                    new PlayerLocation(player), new StaticLocation(goalLocation),
                    Particle.COMPOSTER, 0.1, 0.05, 10, 1, 1
            ).startAnimation();

            double neededYaw = getYaw(goalLocation, player.getLocation());
            double degrees = normalAbsoluteAngleDegrees(Math.toDegrees(neededYaw));
            double playerYaw = player.getLocation().getYaw() + 180;
            String arrow = calculateDirection(playerYaw, degrees);
            String goalLocationStr = arrow + " " + Math.round(goalLocation.getX()) + ", " + Math.round(goalLocation.getY()) + ", " + Math.round(goalLocation.getZ());
            navbb.updateDistance(goalName + " " + goalLocationStr,distance);

            // get vector of the player
            double barLevel = this.bossBarCalculator.calculateBarLevel( navPlayer, goal.getLocation());

            Vector direction = navPlayer.getLocation().getDirection().setY(0);
            Vector towardsEntity = goalLocation.subtract(navPlayer.getLocation()).toVector().normalize();
            double facing = direction.distance(towardsEntity);
            if (facing < 0.4) {
                navbb.setBarColor(BarColor.GREEN);
            } else if(facing > 0.4 && facing < 1.5) {
                navbb.setBarColor(BarColor.YELLOW);
            } else {
                navbb.setBarColor(BarColor.RED);
            }
            //resets goal location cause dumb
            goalLocation.add(navPlayer.getLocation()).toVector().normalize();

            // update the progress on the bar
            navbb.setProgress(barLevel);
        }

        //else create bossbar
        else
        {
            // put the bar on the list
            bblist.put(uuid,bb);

            // create a bar
            bb.createBar(goalName,distance);

            // add player to the bar
            bb.addPlayer(navPlayer);
        }


        if(distance < distance_to_goal)
        {


            // set arrived message
            String primaryColor = messages.getOrDefault("primary_color", "§d");
            String secondaryColor = messages.getOrDefault("secondary_color", "§2");
            String message = primaryColor + messages.getOrDefault("arrived", "You arrived at") + " " + secondaryColor + goalName;

            // send player the message
            navPlayer.sendMessage(message);

            // calculate absolute height
            double height = y_nav-y;
            double absheight = Math.abs(height);

            if(heightCheck)
            {
                // if target is more than 5 levels higher or lower
                if(absheight>5)
                {
                    absheight = round(absheight,2);
                    String messageHeight = secondaryColor + messages.getOrDefault("your_goal", "Your goal is")+" "+absheight+" ";

                    // if lower
                    if(height>0)
                    {
                        messageHeight = messageHeight+messages.getOrDefault("lower", "blocks lower");
                    }

                    // else higher
                    else
                    {
                        messageHeight = messageHeight+messages.getOrDefault("higher", "blocks higher");
                    }

                    navPlayer.sendMessage(messageHeight);
                }
            }

            // delete player at navigating people
            this.playerGoals.removePlayerGoal( uuid );

            // delete the bossbar
            NavBossBar delbb = bblist.get(uuid);
            delbb.delete(navPlayer);

            // remove the bar of the list
            bblist.remove(navPlayer.getUniqueId());
        }
    }
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
    /**
     * Save death locations on death of player
     * **/
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        // if saving deatlocations is true
        if(config.getBoolean("death_location_save"))
        {
            // get the player who died
            Player player = event.getEntity().getPlayer();

            // get his deathlocation
            assert player != null;
            PlayerGoal deathloc = new PlayerGoal("death_location",player);

            FileHandler fileHandler = new FileHandler(plugin,config,messages);
            fileHandler.writeLocationFile(player,deathloc);

            // if needed to automatically start navigation to deathlocation is enabled
            if(config.getBoolean("death_nav"))
            {

                Goal playerGoal = new Goal("death_location",deathloc.getLocation());

                Navigation nav = new Navigation(playerGoals,player,playerGoal,config);
                nav.startNavigation();
            }

        }


    }


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
        if (Math.max(acw, cw) < 4){
            return "§2↑";
        } else if (Math.min(acw, cw) > 15) {
            return "§4↓";
        }else if (acw < cw){
            if (acw <= 4){
                return "§6⬉";
            }else if (acw <= 12){
                return "§4←";
            }else {
                return "§4⬋";
            }
        } else {
            if (cw <= 4) {
                return "§6⬈";
            }else if (cw <= 12){
                return "§4→";
            }else {
                return "§4⬊";
            }
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
