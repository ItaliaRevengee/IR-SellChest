/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitScheduler
 */
package com.italiarevenge.iRSellChest.util.scheduler.schedulers;

import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.util.scheduler.ServerScheduler;
import com.italiarevenge.iRSellChest.util.scheduler.Task;
import com.italiarevenge.iRSellChest.util.scheduler.tasks.BukkitTask;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class BukkitScheduler
implements ServerScheduler {
    private final org.bukkit.scheduler.BukkitScheduler scheduler;

    public BukkitScheduler(AutoSellChests plugin) {
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public void runTask(AutoSellChests plugin, Runnable run) {
        this.scheduler.runTask((Plugin)plugin, run);
    }

    @Override
    public void runTask(AutoSellChests plugin, Chest chest, Runnable run) {
        this.runTask(plugin, run);
    }

    @Override
    public void runTaskLater(AutoSellChests plugin, Runnable run, long delay) {
        this.scheduler.runTaskLater((Plugin)plugin, run, delay);
    }

    @Override
    public void runTaskLater(AutoSellChests plugin, Location loc, Runnable run, long delay) {
        this.runTaskLater(plugin, run, delay);
    }

    @Override
    public Task runTaskTimer(AutoSellChests plugin, Runnable run, long delay, long period) {
        return new BukkitTask(this.scheduler.runTaskTimer((Plugin)plugin, run, delay, period));
    }

    @Override
    public void runTaskAsync(AutoSellChests plugin, Runnable run) {
        this.scheduler.runTaskAsynchronously((Plugin)plugin, run);
    }

    @Override
    public void runTaskLaterAsync(AutoSellChests plugin, Runnable run, long delay) {
        this.scheduler.runTaskLaterAsynchronously((Plugin)plugin, run, delay);
    }

    @Override
    public Task runTaskAsyncTimer(AutoSellChests plugin, Runnable run, long delay, long period) {
        return new BukkitTask(this.scheduler.runTaskTimerAsynchronously((Plugin)plugin, run, delay, period));
    }
}

