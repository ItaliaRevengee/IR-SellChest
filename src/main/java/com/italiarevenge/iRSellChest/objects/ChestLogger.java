package com.italiarevenge.iRSellChest.objects;

import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.util.Logger;
import com.italiarevenge.iRSellChest.util.TimeUtils;

import java.text.ParseException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ChestLogger {

    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicInteger totalItems = new AtomicInteger(0);
    private final AtomicReference<Double> totalProfit = new AtomicReference<>(0.0);

    public ChestLogger(AutoSellChests plugin) {
        long delay;
        try {
            String time = Config.get().getString("console-sold-items-logging.interval");
            delay = TimeUtils.getTime(time);
        } catch (NullPointerException | NumberFormatException | ParseException e) {
            Logger.warn("Failed to load sell logging interval from config.yml, using default of 10 minutes");
            delay = 600000L;
        }
        this.startTask(plugin, delay);
    }

    private void startTask(final AutoSellChests plugin, final long delay) {
        plugin.runTaskAsyncTimer(new Runnable() {
            @Override
            public void run() {
                double profit = totalProfit.getAndSet(0.0);
                int items = totalItems.getAndSet(0);
                Logger.info(Lang.ITEMS_SOLD_CONSOLE_INTERVAL.get()
                        .replace("%profit%", plugin.formatPrices(profit, Lang.ITEMS_SOLD_CONSOLE_INTERVAL.get()))
                        .replace("%count%", String.valueOf(count.get()))
                        .replace("%amount%", String.valueOf(items))
                        .replace("%interval%", TimeUtils.getReadableTime(delay)));
                count.set(0);
            }
        }, delay / 1000L * 20L, delay / 1000L * 20L);
    }

    public void addContents(int items, double profit) {
        this.count.incrementAndGet();
        this.totalItems.addAndGet(items);
        this.totalProfit.updateAndGet(v -> v + profit);
    }
}
