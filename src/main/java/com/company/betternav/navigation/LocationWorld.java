package com.company.betternav.navigation;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationWorld
{
    private String world;
    private String name;
    private int X;
    private int Y;
    private int Z;

    private double distance;

    public LocationWorld(String world, String name, int X, int Y, int Z)
    {
        this.world = world;
        this.name = name;
        this.X = X;
        this.Y = Y;
        this.Z = Z;
//        this.location = new Location(Bukkit.getWorld(world), X, Y, Z);

    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setX(int x) {
        X = x;
    }

    public void setY(int y) {
        Y = y;
    }

    public void setZ(int z) {
        Z = z;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }
    public String getWorld() {
        return world;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public int getZ() {
        return Z;
    }
    public double getDistance() {
        return distance;
    }


//    public Location getLocation() {
//        return location;
//    }
}
