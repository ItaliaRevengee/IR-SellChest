/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.Zrips.CMI.CMI
 *  com.Zrips.CMI.Modules.Display.CMIBillboard
 *  com.Zrips.CMI.Modules.Holograms.CMIHologram
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 */
package com.italiarevenge.iRSellChest.managers.holograms;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Display.CMIBillboard;
import com.Zrips.CMI.Modules.Holograms.CMIHologram;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.italiarevenge.iRSellChest.AutoSellChests;
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

public class CMIHologramHook
implements HologramProvider {
    private final AutoSellChests plugin;
    private static final String H_PREFIX = "ASC_";
    private final List<String> lines;
    private final int display_range;
    private int tickLine;

    public CMIHologramHook(AutoSellChests plugin) {
        this.plugin = plugin;
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
        this.plugin.runTaskAsync(() -> {
            CMIHologram h = CMI.getInstance().getHologramManager().getByName(H_PREFIX + chest.getId());
            if (h == null) {
                this.createHologram(chest);
                return;
            }
            h.enable();
        });
    }

    private CMIHologram createHologram(Chest chest) {
        Location location;
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
            location = new Location(Bukkit.getWorld((String)loc.getLeftLocation().world), x + 0.5, (double)(loc.getLeftLocation().y + 2), z + 0.5);
        } else {
            location = new Location(Bukkit.getWorld((String)loc.getLeftLocation().world), (double)loc.getLeftLocation().x + 0.5, (double)(loc.getLeftLocation().y + 2), (double)loc.getLeftLocation().z + 0.5);
        }
        CMIHologram h = new CMIHologram(H_PREFIX + chest.getId(), location);
        if (this.plugin.version > 118) {
            h.setNewDisplayMethod(true);
            h.setBillboard(CMIBillboard.CENTER);
        }
        h.setLines(this.getLines(chest));
        h.setBackgroundAlpha(50);
        h.setSeeThrough(true);
        h.setShowRange(this.display_range);
        h.enable();
        CMI.getInstance().getHologramManager().addHologram(h);
        return h;
    }

    @Override
    public void unloadHologram(Chest chest) {
        CMIHologram h = CMI.getInstance().getHologramManager().getByName(H_PREFIX + chest.getId());
        if (h != null) {
            h.disable();
        }
    }

    @Override
    public void updateHologram(Chest chest) {
        this.plugin.runTaskAsync(() -> {
            CMIHologram h = CMI.getInstance().getHologramManager().getByName(H_PREFIX + chest.getId());
            if (h != null) {
                h.setLines(this.getLines(chest));
                h.update();
            }
            Logger.debug("Updated hologram for chest " + chest.getId());
        });
    }

    @Override
    public void updateHologramLocation(Chest chest) {
        this.plugin.runTaskAsync(() -> {
            CMIHologram h = CMI.getInstance().getHologramManager().getByName(H_PREFIX + chest.getId());
            if (h == null) {
                return;
            }
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
                h.setLoc(new Location(Bukkit.getWorld((String)loc.getLeftLocation().world), x + 0.5, (double)(loc.getLeftLocation().y + 2), z + 0.5));
            } else {
                h.setLoc(new Location(Bukkit.getWorld((String)loc.getLeftLocation().world), (double)loc.getLeftLocation().x + 0.5, (double)(loc.getLeftLocation().y + 2), (double)loc.getLeftLocation().z + 0.5));
            }
            h.update();
            Logger.debug("Updated hologram location for chest " + chest.getId());
        });
    }

    @Override
    public void tickHologram(Chest chest, String interval) {
        CMIHologram h = CMI.getInstance().getHologramManager().getByName(H_PREFIX + chest.getId());
        if (h != null) {
            h.setLine(this.tickLine, this.lines.get(this.tickLine).replace("%next-interval%", interval));
            h.update();
        }
    }

    @Override
    public void removeHologram(Chest chest) {
        CMIHologram h = CMI.getInstance().getHologramManager().getByName(H_PREFIX + chest.getId());
        if (h != null) {
            h.disable();
            h.remove();
        }
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

