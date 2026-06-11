/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.jetbrains.annotations.NotNull
 */
package com.italiarevenge.iRSellChest.objects;

import com.italiarevenge.iRSellChest.objects.Chest;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class ChestInventory
implements InventoryHolder {
    @NotNull
    public Inventory getInventory() {
        return null;
    }

    public abstract Chest getChest();

    public abstract Location getSelectedChest();

    public abstract boolean isUpdatingInventory();
}

