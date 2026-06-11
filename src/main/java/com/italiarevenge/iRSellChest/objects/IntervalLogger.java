/*
 * Decompiled with CFR 0.152.
 */
package com.italiarevenge.iRSellChest.objects;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.util.Logger;
import com.italiarevenge.iRSellChest.util.TimeUtils;

public class IntervalLogger {
    private final AtomicInteger count = new AtomicInteger(0);
    private final Set<Integer> chests = new HashSet<Integer>();
    private int items;

    public IntervalLogger(ScheduledExecutorService thread) {
        long delay;
        try {
            String time = Config.get().getString("interval-logs.interval");
            delay = TimeUtils.getTime(time);
        }
        catch (NullPointerException | NumberFormatException | ParseException e) {
            Logger.warn("Failed to load sell logging interval from config.yml, got '" + Config.get().getString("interval-logs.interval") + "', using default of 10 minutes");
            delay = 600000L;
        }
        thread.scheduleAtFixedRate(this.getTask(delay), delay, delay, TimeUnit.MILLISECONDS);
        Logger.info("Started interval logger with interval of " + TimeUtils.getReadableTime(delay));
    }

    public Runnable getTask(final long delay) {
        return () -> {
            if (this.items > 0) {
                Logger.info(Lang.ITEMS_SOLD_CONSOLE_INTERVAL.get()
                        .replace("%count%", String.valueOf(this.count.get()))
                        .replace("%items%", String.valueOf(this.items))
                        .replace("%chests%", String.valueOf(this.chests.size()))
                        .replace("%interval%", TimeUtils.getReadableTime(delay)));
                this.chests.clear();
                this.count.set(0);
                this.items = 0;
            }
        };
    }

    public void addContents(int items, int chestID) {
        this.count.incrementAndGet();
        this.items += items;
        this.chests.add(chestID);
    }
}

