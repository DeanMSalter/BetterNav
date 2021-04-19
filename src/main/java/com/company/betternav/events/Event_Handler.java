package com.company.betternav.events;

import be.dezijwegel.betteryaml.BetterYaml;
import com.company.betternav.navigation.Goal;
import com.company.betternav.bossbarcalculators.IBossBarCalculator;
import com.company.betternav.navigation.PlayerGoals;
import com.company.betternav.bossbarcalculators.AdvancedBossbarCalculator;
import com.company.betternav.bossbarcalculators.BasicCalculator;
import com.company.betternav.bossbarcalculators.IdeaBossBarCalculator;
import com.company.betternav.util.animation.SpiralAnimation;
import com.company.betternav.util.animation.location.PlayerLocation;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.UUID;

import static java.lang.String.valueOf;


public class Event_Handler implements Listener {

    private final JavaPlugin plugin;

    private final PlayerGoals playerGoals;
    private HashMap<UUID, NavBossBar> bblist = new HashMap<>();

    private final IBossBarCalculator bossBarCalculator;
    private HashMap<UUID,Boolean> actionbarplayers = new HashMap<>();

    private final YamlConfiguration config;
    private final int distance_to_goal;



    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    public Event_Handler(YamlConfiguration config, PlayerGoals playerGoals, JavaPlugin plugin, HashMap<UUID,Boolean> actionbarplayers,HashMap<UUID,NavBossBar> bblist)
    {
        this.config = config;
        this.playerGoals = playerGoals;
        this.plugin = plugin;
        this.actionbarplayers = actionbarplayers;

        this.bblist = bblist;

        int bbcalc = config.getInt("BossBar");

        if(bbcalc==1){
            this.bossBarCalculator = new IdeaBossBarCalculator();


        }
        else if (bbcalc==2){
            this.bossBarCalculator = new BasicCalculator();
        }
        else{
            this.bossBarCalculator = new AdvancedBossbarCalculator();
        }

        distance_to_goal = config.getInt("Distance");

    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){

        boolean message = config.getBoolean("welcomeMessage");

        Player player = event.getPlayer();
        if(message){

            player.sendMessage(ChatColor.LIGHT_PURPLE + "Betternav plugin enabled: /bn to get help");

        }

        Goal hadNav = playerGoals.getPlayerGoal(player.getUniqueId());
        if(hadNav!=null){

            NavBossBar bb = new NavBossBar(plugin);

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





    //check if player has moved
    @EventHandler
    public void onPlayerWalk(PlayerMoveEvent event){


        Location loc = event.getTo();
        if(loc == null) {
            return;
        }



        //loop over the players that are navigating

        //playersNavigating.forEach((UUID,goal) -> player.sendMessage("User "+UUID +" navigated to" +goal.toString()));

        Player navPlayer = event.getPlayer();
        UUID uuid = navPlayer.getUniqueId();

        // check for action bar

        if (actionbarplayers.containsKey(uuid)){

            // get boolean for player
            boolean actionbar = actionbarplayers.get(uuid);
            if(actionbar){
                int X_Coordinate = navPlayer.getLocation().getBlockX();
                int Y_Coordinate = navPlayer.getLocation().getBlockY();
                int Z_Coordinate = navPlayer.getLocation().getBlockZ();

                String X = valueOf(X_Coordinate);
                String Y = valueOf(Y_Coordinate);
                String Z = valueOf(Z_Coordinate);

                //player.sendMessage("Your current location is X " + X + " Z " + Z);
                navPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.WHITE+"X "+X +"          Y "+ Y + "          Z " + Z));
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
            navPlayer.sendMessage("Target on different world");
            // delete player at navigating people
            this.playerGoals.removePlayerGoal( uuid );

            try {
                // delete the bossbar
                NavBossBar delbb = bblist.get(uuid);
                delbb.delete(navPlayer);

                // remove the bar of the list
                bblist.remove(navPlayer.getUniqueId());
            }catch(Exception e){

            }
            return;
        }

        //get x value
        double x = goal.getLocation().getX();
        //get z value
        double z = goal.getLocation().getZ();

        //navPlayer.sendMessage("UUID "+uuid +" is navigating to " + goalName + " at (" +x +", "+z + ")");


        //get current coordinates
        int x_nav = navPlayer.getLocation().getBlockX();
        int y_nav = navPlayer.getLocation().getBlockY();
        int z_nav = navPlayer.getLocation().getBlockZ();

        //calculate euclidean distance

        double distance = Math.sqrt(Math.pow(((Math.round( x-x_nav ))),2)+(Math.pow(Math.round(z-z_nav),2)));
        distance = round(distance,2);
        //navPlayer.sendMessage(String.valueOf(distance));



        //create new bossbar

        NavBossBar bb = new NavBossBar(plugin);


        //check if player exists
        if(bblist.containsKey(uuid)){

            // get bossbar of player
            NavBossBar navbb = bblist.get(uuid);

            // update the distance to the goal
            navbb.updateDistance(goalName,distance);

            // get vector of the player
            double barLevel = this.bossBarCalculator.calculateBarLevel( navPlayer, goal.getLocation() );

            //System.out.println(angle2);

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

        if(distance < distance_to_goal){

            // set welcome message
            String message = "You arrived at ";

            // set locationname in different color
            String goalMessage = ChatColor.LIGHT_PURPLE + goalName;

            // send player the message
            navPlayer.sendMessage(message + goalMessage);

            // delete player at navigating people
            this.playerGoals.removePlayerGoal( uuid );

            // delete the bossbar
            NavBossBar delbb = bblist.get(uuid);
            delbb.delete(navPlayer);

            // remove the bar of the list
            bblist.remove(navPlayer.getUniqueId());

            // Spawn particle effects when enabled
            if (config.getBoolean("enableAnimations"))
                new SpiralAnimation(
                        new PlayerLocation( navPlayer ),
                        Particle.COMPOSTER,
                        1.3,1.8,5000, 1000,5
                ).startAnimation();

        }

    }

}
