package com.company.betternav;

import be.dezijwegel.betteryaml.BetterLang;
import be.dezijwegel.betteryaml.OptionalBetterYaml;
import be.dezijwegel.betteryaml.validation.ValidationHandler;
import be.dezijwegel.betteryaml.validation.validator.Validator;
import com.company.betternav.commands.CommandsHandler;
import com.company.betternav.events.Event_Handler;
import com.company.betternav.events.NavBossBar;
import com.company.betternav.navigation.Board;
import com.company.betternav.navigation.PlayerGoals;
import com.company.betternav.util.FileHandler;
import com.company.betternav.util.Friend;
import com.company.betternav.util.UpdateChecker;
import com.company.betternav.util.validators.ColorCharValidator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.List;
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
    private static Map<UUID, Board> boards;
    private static Map<UUID, Friend> friendRecords;

    static FileHandler fileHandler;

    static Map<String, String> messages;

    public static FileHandler getFileHandler(){
        return fileHandler;
    }

    public static BetterNav getInstance()
    {
        return instance;
    }
    public static Map<UUID, Board> getBoards() {
        return boards;
    }
    public static void addBoard(Board board) {
        if (board == null){
            return;
        }
        boards.put(board.getPlayer(), board);
    }

    public static Map<String, String> getMessages(){
        return messages;
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

    public static Friend getFriendRecord(UUID owner){
        Friend friend = friendRecords.get(owner);
        if (friend == null){
            Friend newFriendRecord = fileHandler.readFriendFile(owner);
            if (newFriendRecord == null){
                newFriendRecord = new Friend(owner, new HashMap<>());
                fileHandler.writeFriendFile(owner, newFriendRecord);
            }
            friendRecords.put(owner, newFriendRecord);
        }
        return friendRecords.get(owner);
    }
    public static Map<UUID, Friend> getAllFriendRecords(){
        return friendRecords;
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
        Board board = boards.get(playersUUID);
        if (board == null){
            return false;
        } else {
            return board.getStatus();
        }
    }
    public Boolean setBoardStatus(UUID playersUUID, Boolean boardStatus, int numToShow){
        Board board = new Board(boardStatus, playersUUID, numToShow);
        boards.put(playersUUID, board);
        fileHandler.writeBoardFile(playersUUID, board);
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
        BetterNav.boards = new HashMap<>();
        BetterNav.friendRecords = new HashMap<>();

        // get BetterYaml config
        OptionalBetterYaml optionalConfig = new OptionalBetterYaml("config.yml", this, true);
        Optional<YamlConfiguration> optionalYaml = optionalConfig.getYamlConfiguration();

        if (!optionalYaml.isPresent()) {
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "Warning! BetterNav cannot enable");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        YamlConfiguration config = optionalYaml.get();
        messages = getMessages(config);
        fileHandler = new FileHandler(this, config,messages);

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
        getCommand("navlocation").setExecutor(commands);
        getCommand("showcoordinates").setExecutor(commands);
        getCommand("nav").setExecutor(commands);
        getCommand("del").setExecutor(commands);
        getCommand("navplayer").setExecutor(commands);
        getCommand("stopnav").setExecutor(commands);
        getCommand("accept").setExecutor(commands);
        getCommand("deny").setExecutor(commands);
        getCommand("board").setExecutor(commands);
        getCommand("navgeneral").setExecutor(commands);
        getCommand("friends").setExecutor(commands);
        getCommand("friend").setExecutor(commands);
        getCommand("unfriend").setExecutor(commands);
        getCommand("convert").setExecutor(commands);

        // display a plugin enabled message
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "BetterNav plugin enabled");

//        // implement bstats
//        BstatsImplementation bstatsImplementation = new BstatsImplementation(this,config);
//        bstatsImplementation.run();

        //Start UpdateChecker in a seperate thread to not completely block the server
        Thread updateChecker = new UpdateChecker(this);
        updateChecker.start();
        ScoreboardManager manager = Bukkit.getScoreboardManager();
    }

        // run this code when plugin should be disabled
    @Override
    public void onDisable(){
        // display a plugin disabled message
        getServer().getConsoleSender().sendMessage( ChatColor.RED+"BetterNav plugin disabled" );

    }
}