package com.company.betternav.navigation;

import org.bukkit.entity.Player;

import java.util.UUID;

public class Board {

    private boolean status;
    private UUID playerUUID;
    private int numToShow;

    public Board(boolean status, UUID player, int numToShow) {
        this.status = status;
        this.playerUUID = player;
        this.numToShow = numToShow;
    }
    public boolean getStatus() {
        return status;
    }

    public UUID getPlayer() {
        return playerUUID;
    }

    public int getNumToShow() {
        return numToShow;
    }


}
