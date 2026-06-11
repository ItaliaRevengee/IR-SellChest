/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package com.italiarevenge.iRSellChest.objects.upgrades.multipliers;

import com.italiarevenge.iRSellChest.objects.ChestUpgrade;
import com.italiarevenge.iRSellChest.objects.upgrades.PriceMultiplier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DefaultMultiplier
implements PriceMultiplier,
ChestUpgrade {
    private final double multiplier;

    public DefaultMultiplier() {
        this.multiplier = 1.0;
    }

    @Override
    public double getMultiplier() {
        return this.multiplier;
    }

    @Override
    public String getName() {
        return "No multiplier";
    }

    @Override
    public String getLevelName() {
        return "default";
    }

    @Override
    public ItemStack getUpgradeItem(boolean doubleChest) {
        return null;
    }

    @Override
    public boolean buy(Player p, boolean doubleChest) {
        return false;
    }

    @Override
    public String getPrice(boolean doubleChest) {
        return "";
    }
}

