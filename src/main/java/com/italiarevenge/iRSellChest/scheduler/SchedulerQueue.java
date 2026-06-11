/*
 * Decompiled with CFR 0.152.
 */
package com.italiarevenge.iRSellChest.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.managers.UpgradeManager;
import com.italiarevenge.iRSellChest.objects.Chest;

public class SchedulerQueue {
    private final PriorityQueue<Chest> chests = new PriorityQueue<Chest>(Comparator.comparingLong(Chest::getNextInterval));
    private final Map<Integer, List<SellPosition>> SELL_TIMES_PER_INTERVAL = new HashMap<Integer, List<SellPosition>>();

    public SchedulerQueue() {
        this.calculateQueue();
    }

    public void reload() {
        this.chests.clear();
        this.SELL_TIMES_PER_INTERVAL.clear();
        this.calculateQueue();
    }

    private void calculateQueue() {
        for (int i = 0; i < UpgradeManager.getDifferentIntervals(); ++i) {
            this.scheduleFromInterval(i);
        }
    }

    private void scheduleFromInterval(int intervalID) {
        ArrayList<Chest> CHESTS_BY_INTERVAL = new ArrayList<Chest>();
        for (Chest chest : AutoSellChests.getInstance().getManager().getLoadedChests().values()) {
            if ((UpgradeManager.intervalUpgrades ? chest.getIntervalUpgrade() : 0) != intervalID) continue;
            CHESTS_BY_INTERVAL.add(chest);
        }
        if (CHESTS_BY_INTERVAL.isEmpty()) {
            return;
        }
        List<SellPosition> sellTimes = this.schedule(CHESTS_BY_INTERVAL, UpgradeManager.getIntervals()[intervalID]);
        sellTimes.sort(Comparator.comparingLong(SellPosition::sellTime));
        this.SELL_TIMES_PER_INTERVAL.put(intervalID, sellTimes);
    }

    private List<SellPosition> schedule(ArrayList<Chest> CHESTS_BY_INTERVAL, long millis) {
        long currentTime = System.currentTimeMillis();
        long interval = millis / (long)CHESTS_BY_INTERVAL.size();
        ArrayList<SellPosition> sellTimes = new ArrayList<SellPosition>();
        for (int i = 0; i < CHESTS_BY_INTERVAL.size(); ++i) {
            long nextInterval = (long)(i + 1) * interval % millis;
            Chest chest = CHESTS_BY_INTERVAL.get(i);
            chest.setNextInterval(currentTime + nextInterval);
            sellTimes.add(new SellPosition(nextInterval, chest.getId()));
            this.chests.offer(chest);
        }
        sellTimes.add(new SellPosition(millis, -1));
        return sellTimes;
    }

    public Chest getNextAndUpdate() {
        Chest chest = this.chests.poll();
        chest.setNextInterval(System.currentTimeMillis() + chest.getInterval());
        this.chests.offer(chest);
        return chest;
    }

    public Chest peek() {
        return this.chests.peek();
    }

    public void addChest(Chest chest) {
        SellPosition position = new SellPosition(this.getBestSellTime(UpgradeManager.intervalUpgrades ? chest.getIntervalUpgrade() : 0), chest.getId());
        chest.setNextInterval(System.currentTimeMillis() + position.sellTime);
        List<SellPosition> sellTimes = this.getSellTimes(UpgradeManager.intervalUpgrades ? chest.getIntervalUpgrade() : 0);
        sellTimes.add(position);
        sellTimes.sort(Comparator.comparingLong(SellPosition::sellTime));
        this.chests.offer(chest);
    }

    public void removeChest(Chest chest) {
        this.getSellTimes(UpgradeManager.intervalUpgrades ? chest.getIntervalUpgrade() : 0).removeIf(pos -> pos.chestID == chest.getId());
        this.chests.remove(chest);
    }

    public void updateChestInterval(Chest chest, int newIntervalID) {
        this.getSellTimes(chest.getIntervalUpgrade()).removeIf(pos -> pos.chestID == chest.getId());
        this.chests.remove(chest);
        SellPosition position = new SellPosition(this.getBestSellTime(newIntervalID), chest.getId());
        chest.setNextInterval(System.currentTimeMillis() + position.sellTime);
        chest.setInterval(UpgradeManager.getIntervals()[newIntervalID]);
        chest.setIntervalUpgrade(newIntervalID);
        List<SellPosition> sellTimes = this.getSellTimes(newIntervalID);
        sellTimes.add(position);
        sellTimes.sort(Comparator.comparingLong(SellPosition::sellTime));
        this.chests.offer(chest);
    }

    private long getBestSellTime(int newIntervalID) {
        if (((List)this.SELL_TIMES_PER_INTERVAL.getOrDefault(newIntervalID, new ArrayList())).isEmpty()) {
            return UpgradeManager.getIntervals()[newIntervalID] / 2L;
        }
        long largestGap = 0L;
        long previousTime = 0L;
        long previousLargestTime = 0L;
        for (SellPosition position : this.SELL_TIMES_PER_INTERVAL.get(newIntervalID)) {
            long currentGapSize = position.sellTime - previousTime;
            if (currentGapSize > largestGap) {
                previousLargestTime = previousTime;
                largestGap = currentGapSize;
            }
            previousTime = position.sellTime;
        }
        return previousLargestTime + largestGap / 2L;
    }

    private List<SellPosition> getSellTimes(int intervalID) {
        return this.SELL_TIMES_PER_INTERVAL.computeIfAbsent(intervalID, k -> new ArrayList<SellPosition>(Collections.singletonList(new SellPosition(UpgradeManager.getIntervals()[intervalID], -1))));
    }

    private record SellPosition(long sellTime, int chestID) {
    }
}

