/*
 * Decompiled with CFR 0.152.
 */
package com.italiarevenge.iRSellChest.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.objects.ChestUpgrade;
import com.italiarevenge.iRSellChest.objects.upgrades.ChestInterval;
import com.italiarevenge.iRSellChest.objects.upgrades.PriceMultiplier;
import com.italiarevenge.iRSellChest.objects.upgrades.intervals.DefaultInterval;
import com.italiarevenge.iRSellChest.objects.upgrades.intervals.IntervalUpgrade;
import com.italiarevenge.iRSellChest.objects.upgrades.multipliers.DefaultMultiplier;
import com.italiarevenge.iRSellChest.objects.upgrades.multipliers.MultiplierUpgrade;
import com.italiarevenge.iRSellChest.util.Logger;
import com.italiarevenge.iRSellChest.util.exceptions.UpgradeLoadException;

public class UpgradeManager {
    private static final List<ChestInterval> INTERVAL_UPGRADES = new ArrayList<ChestInterval>();
    private static final List<PriceMultiplier> MULTIPLIER_UPGRADES = new ArrayList<PriceMultiplier>();
    public static boolean intervalUpgrades = Config.get().getBoolean("enable-interval-upgrades", true);
    public static boolean multiplierUpgrades = Config.get().getBoolean("enable-multiplier-upgrades", true);

    public UpgradeManager(AutoSellChests plugin) {
        intervalUpgrades = Config.get().getBoolean("enable-interval-upgrades", true);
        multiplierUpgrades = Config.get().getBoolean("enable-multiplier-upgrades", true);
        if (intervalUpgrades) {
            this.loadIntervalUpgrades();
        } else {
            INTERVAL_UPGRADES.add(new DefaultInterval());
        }
        if (multiplierUpgrades) {
            this.loadMultiplierUpgrades();
        } else {
            MULTIPLIER_UPGRADES.add(new DefaultMultiplier());
        }
    }

    public void reload() {
        INTERVAL_UPGRADES.clear();
        MULTIPLIER_UPGRADES.clear();
        intervalUpgrades = Config.get().getBoolean("enable-interval-upgrades", true);
        multiplierUpgrades = Config.get().getBoolean("enable-multiplier-upgrades", true);
        if (intervalUpgrades) {
            this.loadIntervalUpgrades();
        } else {
            INTERVAL_UPGRADES.add(new DefaultInterval());
        }
        if (multiplierUpgrades) {
            this.loadMultiplierUpgrades();
        } else {
            MULTIPLIER_UPGRADES.add(new DefaultMultiplier());
        }
    }

    private void loadIntervalUpgrades() {
        Map<Integer, String> levels = this.getLevels("interval-upgrades");
        ArrayList<Integer> order = new ArrayList<Integer>(levels.keySet());
        Collections.sort(order);
        int lvl = 0;
        Iterator iterator = order.iterator();
        while (iterator.hasNext()) {
            int i = (Integer)iterator.next();
            try {
                INTERVAL_UPGRADES.add(new IntervalUpgrade(Config.get().getConfigurationSection("interval-upgrades." + levels.get(i)), lvl++));
            }
            catch (UpgradeLoadException e) {
                Logger.warn("Failed to enable interval upgrade '" + levels.get(i) + "': " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (!INTERVAL_UPGRADES.isEmpty()) {
            Logger.info("Completed loading " + INTERVAL_UPGRADES.size() + " interval upgrade(s) with weight: " + order.stream().map(String::valueOf).collect(Collectors.joining(" -> ")));
        }
    }

    private void loadMultiplierUpgrades() {
        Map<Integer, String> levels = this.getLevels("multiplier-upgrades");
        ArrayList<Integer> order = new ArrayList<Integer>(levels.keySet());
        Collections.sort(order);
        int lvl = 0;
        Iterator iterator = order.iterator();
        while (iterator.hasNext()) {
            int i = (Integer)iterator.next();
            try {
                MULTIPLIER_UPGRADES.add(new MultiplierUpgrade(Config.get().getConfigurationSection("multiplier-upgrades." + levels.get(i)), lvl++));
            }
            catch (UpgradeLoadException e) {
                Logger.warn("Failed to enable multiplier upgrade '" + levels.get(i) + "': " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (!MULTIPLIER_UPGRADES.isEmpty()) {
            Logger.info("Completed loading " + MULTIPLIER_UPGRADES.size() + " multiplier upgrade(s) with weight: " + order.stream().map(String::valueOf).collect(Collectors.joining(" -> ")));
        }
    }

    private Map<Integer, String> getLevels(String path) {
        HashMap<Integer, String> levels = new HashMap<Integer, String>();
        for (String upgrade : Config.get().getConfigurationSection(path).getKeys(false)) {
            int weight = Config.get().getInt(path + "." + upgrade + ".weight");
            if (levels.containsKey(weight)) {
                Logger.warn("Failed to load upgrade '" + path + "." + upgrade + "': There cannot be two upgrades with weight " + weight);
                continue;
            }
            levels.put(weight, upgrade);
        }
        return levels;
    }

    public static ChestUpgrade getIntervalUpgrade(int upgrade) {
        try {
            return (ChestUpgrade)((Object)INTERVAL_UPGRADES.get(upgrade));
        }
        catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            return null;
        }
    }

    public static ChestUpgrade getMultiplierUpgrade(int upgrade) {
        try {
            return (ChestUpgrade)((Object)MULTIPLIER_UPGRADES.get(upgrade));
        }
        catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            return null;
        }
    }

    public static Long[] getIntervalsInTicks() {
        return (Long[])INTERVAL_UPGRADES.stream().map(ChestInterval::getTicks).toArray(Long[]::new);
    }

    public static Long[] getIntervals() {
        return (Long[])INTERVAL_UPGRADES.stream().map(ChestInterval::getInterval).toArray(Long[]::new);
    }

    public static int getDifferentIntervals() {
        return INTERVAL_UPGRADES.size();
    }

    public static Double[] getMultipliers() {
        return (Double[])MULTIPLIER_UPGRADES.stream().map(PriceMultiplier::getMultiplier).toArray(Double[]::new);
    }
}

