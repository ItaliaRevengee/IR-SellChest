/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 */
package com.italiarevenge.iRSellChest.util.scheduler;

import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.util.scheduler.Task;
import org.bukkit.Location;

public interface ServerScheduler {
    public void runTask(AutoSellChests var1, Runnable var2);

    public void runTask(AutoSellChests var1, Chest var2, Runnable var3);

    public void runTaskLater(AutoSellChests var1, Runnable var2, long var3);

    public void runTaskLater(AutoSellChests var1, Location var2, Runnable var3, long var4);

    public Task runTaskTimer(AutoSellChests var1, Runnable var2, long var3, long var5);

    public void runTaskAsync(AutoSellChests var1, Runnable var2);

    public void runTaskLaterAsync(AutoSellChests var1, Runnable var2, long var3);

    public Task runTaskAsyncTimer(AutoSellChests var1, Runnable var2, long var3, long var5);
}

