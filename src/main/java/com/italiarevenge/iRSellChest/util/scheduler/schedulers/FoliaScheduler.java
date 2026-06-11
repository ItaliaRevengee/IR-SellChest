/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.threadedregions.scheduler.AsyncScheduler
 *  io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
 *  io.papermc.paper.threadedregions.scheduler.RegionScheduler
 *  org.bukkit.Location
 *  org.bukkit.plugin.Plugin
 */
package com.italiarevenge.iRSellChest.util.scheduler.schedulers;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import java.util.concurrent.TimeUnit;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.util.scheduler.ServerScheduler;
import com.italiarevenge.iRSellChest.util.scheduler.Task;
import com.italiarevenge.iRSellChest.util.scheduler.tasks.FoliaTask;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class FoliaScheduler
implements ServerScheduler {
    private final GlobalRegionScheduler globalRegionScheduler;
    private final RegionScheduler regionScheduler;
    private final AsyncScheduler asyncScheduler;

    public FoliaScheduler(AutoSellChests plugin) {
        this.globalRegionScheduler = plugin.getServer().getGlobalRegionScheduler();
        this.regionScheduler = plugin.getServer().getRegionScheduler();
        this.asyncScheduler = plugin.getServer().getAsyncScheduler();
    }

    @Override
    public void runTask(AutoSellChests plugin, Runnable run) {
        this.globalRegionScheduler.run((Plugin)plugin, t -> run.run());
    }

    @Override
    public void runTask(AutoSellChests plugin, Chest chest, Runnable run) {
        this.regionScheduler.run((Plugin)plugin, chest.getLocation().getLeftLocation().toLoc(), t -> run.run());
    }

    public void runTask(AutoSellChests plugin, Location loc, Runnable run) {
        this.regionScheduler.run((Plugin)plugin, loc, t -> run.run());
    }

    @Override
    public void runTaskLater(AutoSellChests plugin, Runnable run, long delay) {
        if (delay <= 0L) {
            this.runTask(plugin, run);
        }
        this.globalRegionScheduler.runDelayed((Plugin)plugin, t -> run.run(), delay);
    }

    @Override
    public void runTaskLater(AutoSellChests plugin, Location loc, Runnable run, long delay) {
        if (delay <= 0L) {
            this.runTask(plugin, loc, run);
        }
        this.regionScheduler.runDelayed((Plugin)plugin, loc, t -> run.run(), delay);
    }

    @Override
    public Task runTaskTimer(AutoSellChests plugin, Runnable run, long delay, long period) {
        if (delay <= 0L) {
            delay = 1L;
        }
        return new FoliaTask(this.globalRegionScheduler.runAtFixedRate((Plugin)plugin, t -> run.run(), delay, period));
    }

    @Override
    public void runTaskAsync(AutoSellChests plugin, Runnable run) {
        this.asyncScheduler.runNow((Plugin)plugin, t -> run.run());
    }

    @Override
    public void runTaskLaterAsync(AutoSellChests plugin, Runnable run, long delay) {
        if (delay <= 0L) {
            this.runTaskAsync(plugin, run);
        }
        this.asyncScheduler.runDelayed((Plugin)plugin, t -> run.run(), delay * 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public Task runTaskAsyncTimer(AutoSellChests plugin, Runnable run, long delay, long period) {
        if (delay <= 0L) {
            delay = 1L;
        }
        return new FoliaTask(this.asyncScheduler.runAtFixedRate((Plugin)plugin, t -> run.run(), delay * 50L, period * 50L, TimeUnit.MILLISECONDS));
    }
}

