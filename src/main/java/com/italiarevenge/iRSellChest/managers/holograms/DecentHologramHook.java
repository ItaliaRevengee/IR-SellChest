/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  eu.decentsoftware.holograms.api.DHAPI
 *  eu.decentsoftware.holograms.api.holograms.Hologram
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 */
package com.italiarevenge.iRSellChest.managers.holograms;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.managers.HologramProvider;
import com.italiarevenge.iRSellChest.managers.UpgradeManager;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.objects.ChestLocation;
import com.italiarevenge.iRSellChest.util.Logger;
import com.italiarevenge.iRSellChest.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class DecentHologramHook
implements HologramProvider {
    private static final String H_PREFIX = "ASC_";
    private final List<String> lines;
    private final int display_range;
    private int tickLine;

    public DecentHologramHook() {
        List lines = Config.get().getStringList("chest-holograms.lines");
        for (int i = 0; i < lines.size(); ++i) {
            if (!((String)lines.get(i)).contains("%next-interval%")) continue;
            this.tickLine = i;
        }
        this.lines = lines.stream().map(s -> Lang.formatColors(s, null)).collect(Collectors.toList());
        this.display_range = Config.get().getInt("chest-holograms.display-range");
    }

    @Override
    public void loadHologram(Chest chest) {
        Hologram h = DHAPI.getHologram((String)(H_PREFIX + chest.getId()));
        if (h == null) {
            this.createHologram(chest);
            return;
        }
        h.enable();
    }

    private Hologram createHologram(Chest chest) {
        Hologram h;
        ChestLocation loc = chest.getLocation();
        if (loc.isDoubleChest()) {
            double x = loc.getLeftLocation().x;
            double z = loc.getLeftLocation().z;
            if (x != (double)loc.getRightLocation().x) {
                x += x > (double)loc.getRightLocation().x ? -0.5 : 0.5;
            }
            if (z != (double)loc.getRightLocation().z) {
                z += z > (double)loc.getRightLocation().z ? -0.5 : 0.5;
            }
            h = DHAPI.createHologram((String)(H_PREFIX + chest.getId()), (Location)new Location(Bukkit.getWorld((String)loc.getLeftLocation().world), x + 0.5, (double)(loc.getLeftLocation().y + 2), z + 0.5), (boolean)false, this.getLines(chest));
        } else {
            h = DHAPI.createHologram((String)(H_PREFIX + chest.getId()), (Location)new Location(Bukkit.getWorld((String)loc.getLeftLocation().world), (double)loc.getLeftLocation().x + 0.5, (double)(loc.getLeftLocation().y + 2), (double)loc.getLeftLocation().z + 0.5), (boolean)false, this.getLines(chest));
        }
        h.setDisplayRange(this.display_range);
        Logger.debug("Created hologram for chest " + chest.getId());
        h.enable();
        return h;
    }

    @Override
    public void unloadHologram(Chest chest) {
        Hologram h = DHAPI.getHologram((String)(H_PREFIX + chest.getId()));
        if (h != null) {
            h.disable();
        }
    }

    @Override
    public void updateHologram(Chest chest) {
        Hologram h = DHAPI.getHologram((String)(H_PREFIX + chest.getId()));
        int i = 0;
        for (String s : this.getLines(chest)) {
            h.getPage(0).setLine(i++, s);
        }
        Logger.debug("Updated hologram for chest " + chest.getId());
    }

    @Override
    public void updateHologramLocation(Chest chest) {
        ChestLocation loc = chest.getLocation();
        if (loc.isDoubleChest()) {
            double x = loc.getLeftLocation().x;
            double z = loc.getLeftLocation().z;
            if (x != (double)loc.getRightLocation().x) {
                x += x > (double)loc.getRightLocation().x ? -0.5 : 0.5;
            }
            if (z != (double)loc.getRightLocation().z) {
                z += z > (double)loc.getRightLocation().z ? -0.5 : 0.5;
            }
            DHAPI.moveHologram((String)(H_PREFIX + chest.getId()), (Location)new Location(Bukkit.getWorld((String)loc.getLeftLocation().world), x + 0.5, (double)(loc.getLeftLocation().y + 2), z + 0.5));
        } else {
            DHAPI.moveHologram((String)(H_PREFIX + chest.getId()), (Location)new Location(Bukkit.getWorld((String)loc.getLeftLocation().world), (double)loc.getLeftLocation().x + 0.5, (double)(loc.getLeftLocation().y + 2), (double)loc.getLeftLocation().z + 0.5));
        }
        Logger.debug("Updated hologram location for chest " + chest.getId());
    }

    @Override
    public void tickHologram(Chest chest, String interval) {
        Hologram h = DHAPI.getHologram((String)(H_PREFIX + chest.getId()));
        if (h == null) {
            h = this.createHologram(chest);
        }
        if (h.isDisabled()) {
            return;
        }
        DHAPI.setHologramLine((Hologram)h, (int)this.tickLine, (String)this.lines.get(this.tickLine).replace("%next-interval%", interval));
    }

    @Override
    public void removeHologram(Chest chest) {
        DHAPI.removeHologram((String)(H_PREFIX + chest.getId()));
    }

    private ArrayList<String> getLines(Chest chest) {
        ArrayList<String> lines = new ArrayList<String>(this.lines);
        for (int i = 0; i < lines.size(); ++i) {
            String l = lines.get(i);
            lines.set(i, l.replace("%next-interval%", TimeUtils.getReadableTime(chest.getNextInterval() - (System.currentTimeMillis() - 1000L))).replace("%chest-name%", chest.getName()).replace("%multiplier-name%", UpgradeManager.multiplierUpgrades ? UpgradeManager.getMultiplierUpgrade(chest.getMultiplierUpgrade()).getName() : "").replace("%multiplier-level%", UpgradeManager.multiplierUpgrades ? UpgradeManager.getMultiplierUpgrade(chest.getMultiplierUpgrade()).getLevelName() : "").replace("%interval-name%", UpgradeManager.intervalUpgrades ? UpgradeManager.getIntervalUpgrade(chest.getIntervalUpgrade()).getName() : "").replace("%interval-level%", UpgradeManager.intervalUpgrades ? UpgradeManager.getIntervalUpgrade(chest.getIntervalUpgrade()).getLevelName() : ""));
        }
        return lines;
    }
}

