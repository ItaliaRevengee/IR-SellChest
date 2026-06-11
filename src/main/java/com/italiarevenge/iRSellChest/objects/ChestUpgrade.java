/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package com.italiarevenge.iRSellChest.objects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ChestUpgrade {
    public String getName();

    public String getLevelName();

    public ItemStack getUpgradeItem(boolean var1);

    public boolean buy(Player var1, boolean var2);

    public String getPrice(boolean var1);
}

