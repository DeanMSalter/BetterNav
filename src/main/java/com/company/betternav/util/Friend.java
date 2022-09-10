package com.company.betternav.util;

import com.company.betternav.BetterNav;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Friend {

    UUID owner;
    Map<UUID, String> friends;
    List<UUID> outgoingFriendRequests;

    public Friend(UUID owner, Map<UUID, String> friends) {
        this.owner = owner;
        this.friends = friends;
        this.outgoingFriendRequests = new ArrayList<>();
    }

    public Map<UUID, String> getFriends(){
        return friends;
    }

    public String getFriendName(UUID friendId){
        return friends.get(friendId);
    }
    public UUID getFriendID(String friendName){
        for (UUID uuid : friends.keySet()) {
            if (friends.get(uuid).equals(friendName)){
                return uuid;
            }
        }
        return null;
    }
    public Boolean isFriend(UUID friendID){
        return friends.get(friendID) != null ? true : false;
    }
    public Boolean isFriend(String friendName){
        return getFriendID(friendName) != null ? true : false;
    }

    public void addFriend( UUID accepter){
        Player newFriend = Bukkit.getPlayer(accepter);
        if (newFriend == null){
            throw new RuntimeException("could not find player to add as a friend" + accepter);
        }
        friends.put(accepter, newFriend.getName());
        outgoingFriendRequests.remove(accepter);
        BetterNav.getFileHandler().writeFriendFile(owner, this);

    }
    public void removeFriend(UUID friend){
        friends.remove(friend);
        BetterNav.getFileHandler().writeFriendFile(owner, this);
    }
    public void removeFriend(String friend){
        for (UUID uuid : friends.keySet()) {
            if (friends.get(uuid).equals(friend)){
                removeFriend(uuid);
                BetterNav.getFileHandler().writeFriendFile(owner, this);
                return;
            }
        }
        Bukkit.getPlayer(owner).sendMessage("Could not find player to unfriend: " + friend);

    }


    public void addFriendRequest(UUID newFriend){
        outgoingFriendRequests.add(newFriend);
    }
    public boolean removeFriendRequest(UUID newFriend){
       return outgoingFriendRequests.remove(newFriend);
    }
    public void removeAllFriendRequests(){
        outgoingFriendRequests.clear();
    }
    public List<UUID> getFriendRequests(){
        return outgoingFriendRequests;
    }
}
