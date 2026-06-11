/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package com.italiarevenge.iRSellChest.objects.upgrades.intervals;

import java.text.ParseException;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.objects.ChestUpgrade;
import com.italiarevenge.iRSellChest.objects.upgrades.ChestInterval;
import com.italiarevenge.iRSellChest.util.Logger;
import com.italiarevenge.iRSellChest.util.TimeUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DefaultInterval
implements ChestInterval,
ChestUpgrade {
    private final long interval = this.getSellInterval();
    private final long ticks = this.interval / 1000L * 20L;

    @Override
    public long getInterval() {
        return this.interval;
    }

    @Override
    public long getTicks() {
        return this.ticks;
    }

    private long getSellInterval() {
        try {
            return TimeUtils.getTime(Config.get().getString("autosell-interval"));
        }
        catch (NullPointerException | ParseException e) {
            Logger.warn("Could not read the 'autosell-interval' from config, using 10 minutes as default.");
            e.printStackTrace();
            return 600000L;
        }
    }

    @Override
    public String getName() {
        return "Default interval";
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

