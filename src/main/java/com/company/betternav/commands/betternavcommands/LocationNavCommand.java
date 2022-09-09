package com.company.betternav.commands.betternavcommands;

import com.company.betternav.BetterNav;
import com.company.betternav.commands.BetterNavCommand;
import com.company.betternav.navigation.Goal;
import com.company.betternav.navigation.Navigation;
import com.company.betternav.navigation.PlayerGoals;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class LocationNavCommand extends BetterNavCommand {

    private final YamlConfiguration config;
    private final PlayerGoals playerGoals;

    private final BetterNav betterNav;

    public LocationNavCommand(YamlConfiguration config, PlayerGoals playerGoals, BetterNav betterNav) {
        this.config = config;
        this.playerGoals = playerGoals;
        this.betterNav = betterNav;
    }

    @Override
    public boolean execute(Player player, Command cmd, String s, String[] args, Map<String, String> messages) {
        try {
            if (args.length < 3) {
                player.sendMessage("Coordiantes not provided");
                return true;
            }
            String xCoord = args[0];
            String yCoord = args[1];
            String zCoord = args[2];

            if (xCoord == null) {
                player.sendMessage("x Coordiante not provided");
                return true;
            }
            if (yCoord == null) {
                player.sendMessage("y Coordiante not provided");
                return true;
            }
            if (zCoord == null) {
                player.sendMessage("z Coordiante not provided");
                return true;
            }

            //get coordinates to the goal
            String goal = xCoord + " " + yCoord + " " + zCoord;
            double x = Double.parseDouble(xCoord);
            double y = Double.parseDouble(yCoord);
            double z = Double.parseDouble(zCoord);

            Goal playerGoal = new Goal(goal, new Location(Bukkit.getWorld(player.getWorld().getName()), x, y, z));

            // send message to player
            String primaryColor = messages.getOrDefault("primary_color", "§d");
            String secondaryColor = messages.getOrDefault("secondary_color", "§2");

            String message = primaryColor + messages.getOrDefault("navigating_to", "Navigating to") + " " + goal;

            // only send x and z if height check is not enabled
            if (config.getBoolean("height_check")) {
                message = message + secondaryColor + " " + x + " " + y + " " + z;
            } else {
                message = message + secondaryColor + " " + x + " " + z;
            }

            player.sendMessage(message);

            Navigation nav = new Navigation(playerGoals, player, playerGoal, config);
            nav.startNavigation();

                /*this.playerGoals.addPlayerGoal(PlayersUUID, playerGoal);

                if (config.getBoolean("enableAnimations"))
                    new LineAnimation(
                            new PlayerLocation(player), new StaticLocation(playerGoal.getLocation()),
                            Particle.COMPOSTER, 7.0, 0.05, 0.5, 500, 3
                    ).startAnimation();

                 */

        } catch (IllegalArgumentException e) {
            player.sendMessage(messages.getOrDefault("error", "/bn to get information about how to use Betternav commands"));
        }
        return true;
    }
}
