/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 */
package com.italiarevenge.iRSellChest.objects;

import java.util.Objects;
import org.bukkit.Bukkit;

public class Location {
    public final String world;
    public final int x;
    public final int y;
    public final int z;

    public Location(org.bukkit.Location loc) {
        this.world = loc.getWorld().getName();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }

    public Location(String location) {
        this.world = location.split(":")[0];
        this.x = Integer.parseInt(location.split(":")[1]);
        this.y = Integer.parseInt(location.split(":")[2]);
        this.z = Integer.parseInt(location.split(":")[3]);
    }

    public org.bukkit.Location toLoc() {
        return new org.bukkit.Location(Bukkit.getWorld((String)this.world), (double)this.x, (double)this.y, (double)this.z);
    }

    public String toString() {
        return this.world + ":" + this.x + ":" + this.y + ":" + this.z;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Location) {
            Location loc = (Location)object;
            return this.world.equals(loc.world) && this.x == loc.x && this.y == loc.y && this.z == loc.z;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.world, this.x, this.y, this.z);
    }
}

