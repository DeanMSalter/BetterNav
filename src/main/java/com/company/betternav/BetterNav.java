package com.company.betternav;

import be.dezijwegel.betteryaml.BetterLang;
import be.dezijwegel.betteryaml.OptionalBetterYaml;
import be.dezijwegel.betteryaml.validation.ValidationHandler;
import be.dezijwegel.betteryaml.validation.validator.Validator;
import com.company.betternav.commands.CommandsHandler;
import com.company.betternav.events.Event_Handler;
import com.company.betternav.events.NavBossBar;
import com.company.betternav.navigation.PlayerGoals;
import com.company.betternav.util.FileHandler;
import com.company.betternav.util.UpdateChecker;
import com.company.betternav.util.validators.ColorCharValidator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
* BetterNav plugin
* @author Thomas Verschoor & Dieter Nuytemans
**/

public class BetterNav extends JavaPlugin {

    private static BetterNav instance;

    private static Map<UUID, UUID> navigationRequests;
    private static Map<UUID, Boolean> boardStatuses;

    public static BetterNav getInstance()
    {
        return instance;
    }

    // BetterLang implementation
    public Map<String,String> getMessages(YamlConfiguration config){
        // get language out of config file
        String language = config.getString("language");

        Validator colorValidator = new ColorCharValidator();
        ValidationHandler validation = new ValidationHandler()
                .addValidator("primary_color", colorValidator)
                .addValidator("secondary_color", colorValidator)
                .addValidator("usage",colorValidator)
                .addValidator("savelocation_current",colorValidator)
                .addValidator("savelocation_coordinates",colorValidator)
                .addValidator("actionbar_color",colorValidator)
                .addValidator("location_saved",colorValidator)
                .addValidator("or", colorValidator);

        // Auto-updates the config on the server and loads a YamlConfiguration and File. Optionally, a boolean can be passed, which enables or disables logging.
        BetterLang messaging = new BetterLang("messages.yml", language+".yml", validation, this);

        // Get all message names and their mapped messages. Useful when sending named messages to players (eg: see below)
        return messaging.getMessages();
    }

    public void addRequest(UUID requester, UUID accepter){
        navigationRequests.put(requester, accepter);
    }
    public void removeRequest(UUID requester){
        navigationRequests.remove(requester);
    }
    public void declineAcceptRequest(UUID accepter){
        //TODO: investigate if memory leak
        while (navigationRequests.values().remove(accepter));
    }
    public UUID getNavigationByRequester(UUID requester){
        return navigationRequests.get(requester);
    }
    public UUID getNavigationByAccepter(UUID accepter){
        return getKeyByValue(navigationRequests, accepter);
    }

    public Boolean getBoardStatus(UUID playersUUID) {
        Boolean boardStatus = boardStatuses.get(playersUUID);
        if (boardStatus == null){
            boardStatus = false;
        }
        return boardStatus;
    }
    public Boolean setBoardStatus(UUID playersUUID, Boolean boardStatus){
        boardStatuses.put(playersUUID, boardStatus);
        return getBoardStatus(playersUUID);
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
    // run this code when plugin is started
    @Override
    public void onEnable() {

        BetterNav.instance = this;
        BetterNav.navigationRequests = new HashMap<>();
        BetterNav.boardStatuses = new HashMap<>();


        // get BetterYaml config
        OptionalBetterYaml optionalConfig = new OptionalBetterYaml("config.yml", this, true);
        Optional<YamlConfiguration> optionalYaml = optionalConfig.getYamlConfiguration();

        if (!optionalYaml.isPresent()) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "Warning! BetterNav cannot enable");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        YamlConfiguration config = optionalYaml.get();
        Map<String, String> messages = getMessages(config);
        FileHandler fileHandler = new FileHandler(this, config,messages);

        final PlayerGoals playerGoals = new PlayerGoals();
        final HashMap<UUID, Boolean> actionbarplayers = new HashMap<>();
        final HashMap<UUID, NavBossBar> bblist = new HashMap<>();

        // start command handler
        CommandsHandler commands = new CommandsHandler(config, playerGoals, instance, actionbarplayers, bblist, messages);
        getServer().getPluginManager().registerEvents(new Event_Handler(config, playerGoals, this, actionbarplayers, bblist, messages, fileHandler), this);

        // set executor for the commands
        getCommand("bn").setExecutor(commands);
        getCommand("getlocation").setExecutor(commands);
        getCommand("savelocation").setExecutor(commands);
        getCommand("showlocations").setExecutor(commands);
        getCommand("showcoordinates").setExecutor(commands);
        getCommand("nav").setExecutor(commands);
        getCommand("del").setExecutor(commands);
        getCommand("navplayer").setExecutor(commands);
        getCommand("stopnav").setExecutor(commands);
        getCommand("accept").setExecutor(commands);
        getCommand("deny").setExecutor(commands);
        getCommand("board").setExecutor(commands);

        // display a plugin enabled message
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "BetterNav plugin enabled");

//        // implement bstats
//        BstatsImplementation bstatsImplementation = new BstatsImplementation(this,config);
//        bstatsImplementation.run();

        //Start UpdateChecker in a seperate thread to not completely block the server
        Thread updateChecker = new UpdateChecker(this);
        updateChecker.start();
        ScoreboardManager manager = Bukkit.getScoreboardManager();

//        Bukkit.getScheduler().runTaskTimer(this, () -> {
//            boardStatuses.forEach((uuid, status) -> {
//                Player player = Bukkit.getPlayer(uuid);
//                if (player == null) {
//                    return;
//                }
//                Scoreboard board = manager.getNewScoreboard();
//                Objective objective = board.getObjective("locations");
//                if (!status && objective != null){
//                    objective.unregister();
//                }
//                if (!status){
//                    return;
//                }
//                if (objective == null){
//                    objective = board.registerNewObjective("locations", "", "Locations");
//                }
//
//
//                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
//                objective.setDisplayName("Locations");
//                List<LocationWorld> locations = fileHandler.getLocationsInWorld(player.getWorld(), player);
//                for (LocationWorld coordinates : locations) {
//                    Location location = new Location( Bukkit.getWorld(coordinates.getWorld()), coordinates.getX(), coordinates.getY(), coordinates.getZ());
//                    double neededYaw = getYaw(location, player.getLocation());
//                    double degrees = normalAbsoluteAngleDegrees(Math.toDegrees(neededYaw));
//                    double playerYaw = player.getLocation().getYaw() + 180;
//
//                    Vector direction = player.getLocation().getDirection();
//                    Vector towardsEntity = location.subtract(player.getLocation()).toVector().normalize();
//                    String arrow = calculateDirection(playerYaw, degrees);
//
//                    double facingDifference = direction.distance(towardsEntity);
//                    if (facingDifference < 0.2) {
//                        arrow = "§2" + arrow;
//                    } else if(facingDifference > 0.2 && facingDifference < 1.5) {
//                        arrow = "§6" + arrow;
//                    } else {
//                        arrow = "§4" + arrow;
//                    }
//
//                    Score score = objective.getScore(arrow + ChatColor.GREEN + coordinates.getName());
//                    score.setScore((int) player.getLocation().distance(location));
//                }
//                player.setScoreboard(board);
//            });
//        }, 10, 40);
    }

        // run this code when plugin should be disabled
    @Override
    public void onDisable(){
        // display a plugin disabled message
        getServer().getConsoleSender().sendMessage( ChatColor.RED+"BetterNav plugin disabled" );

    }

//    private static String calculateDirection(double playersYaw, double locationYaw){
//        int cw = 0;
//        double cwPlayersYaw = playersYaw;
//        while(Math.abs(cwPlayersYaw - locationYaw) > 10){
//            if(cwPlayersYaw >= 360) {
//                cwPlayersYaw = 0;
//            }
//            cwPlayersYaw += 10;
//            cw++;
//            if (cw > 36){
//                break;
//            }
//        }
//        int acw = 0;
//        double acwPlayersYaw = playersYaw;
//        while(Math.abs(acwPlayersYaw - locationYaw) > 10){
//            if(acwPlayersYaw <= 0) {
//                acwPlayersYaw = 360;
//            }
//            acwPlayersYaw -= 10;
//            acw++;
//            if (acw > 36){
//                break;
//            }
//        }
//        if (Math.max(acw, cw) < 3){
//            return "↑";
//        } else if (Math.min(acw, cw) > 15) {
//            return "↓";
//        }else if (acw < cw){
//            if (acw <= 4){
//                return "⬉";
//            }else if (acw <= 12){
//                return "←";
//            }else {
//                return "⬋";
//            }
//        } else {
//            if (cw <= 4) {
//                return "⬈";
//            }else if (cw <= 12){
//                return "→";
//            }else {
//                return "⬊";
//            }
//        }
//    }
//
//    public double getYaw(Location locA, Location locB) {
//        Vector dir  = locB.subtract(locA).toVector();
//        dir.setY(0);
//        dir = dir.normalize();
//        return dir.getX() > 0 ? -Math.acos(dir.getZ()) : Math.acos(dir.getZ());
//    }
//    public static double normalAbsoluteAngleDegrees(double angle) {
//        return (angle %= 360) >= 0 ? angle : (angle + 360);
//    }
}