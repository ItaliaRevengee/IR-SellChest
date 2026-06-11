/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  de.oliver.fancyholograms.api.FancyHologramsPlugin
 *  de.oliver.fancyholograms.api.HologramManager
 *  de.oliver.fancyholograms.api.data.HologramData
 *  de.oliver.fancyholograms.api.data.TextHologramData
 *  de.oliver.fancyholograms.api.hologram.Hologram
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.entity.Display$Billboard
 */
package com.italiarevenge.iRSellChest.managers.holograms;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.HologramData;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
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
import org.bukkit.entity.Display;

public class FancyHologramHook
implements HologramProvider {
    private static final String H_PREFIX = "ASC_";
    private final HologramManager manager = FancyHologramsPlugin.get().getHologramManager();
    private final List<String> lines;
    private final int display_range;
    private int tickLine;

    public FancyHologramHook() {
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
        Hologram h = this.manager.getHologram(H_PREFIX + chest.getId()).orElse(null);
        if (h == null) {
            this.createHologram(chest);
            return;
        }
        HologramData hologramData = h.getData();
        if (hologramData instanceof TextHologramData) {
            TextHologramData textData = (TextHologramData)hologramData;
            textData.setText(this.getLines(chest));
            h.queueUpdate();
        }
    }

    private Hologram createHologram(Chest chest) {
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
            location = new Location(Bukkit.getWorld((String)loc.getLeftLocation().world), x + 0.5, (double)loc.getLeftLocation().y + 1.35, z + 0.5);
        } else {
            location = new Location(Bukkit.getWorld((String)loc.getLeftLocation().world), (double)loc.getLeftLocation().x + 0.5, (double)loc.getLeftLocation().y + 1.35, (double)loc.getLeftLocation().z + 0.5);
        }
        TextHologramData data = new TextHologramData(H_PREFIX + chest.getId(), location);
        if (AutoSellChests.getInstance().version > 118) {
            data.setBillboard(Display.Billboard.CENTER);
        }
        data.setText(this.getLines(chest));
        data.setSeeThrough(true);
        data.setPersistent(false);
        data.setVisibilityDistance(this.display_range);
        Hologram hologram = this.manager.create((HologramData)data);
        this.manager.addHologram(hologram);
        return hologram;
    }

    @Override
    public void unloadHologram(Chest chest) {
        Hologram hologram = this.manager.getHologram(H_PREFIX + chest.getId()).orElse(null);
        if (hologram == null) {
            return;
        }
        this.manager.removeHologram(hologram);
    }

    @Override
    public void updateHologram(Chest chest) {
        Hologram hologram = this.manager.getHologram(H_PREFIX + chest.getId()).orElse(null);
        if (hologram == null) {
            return;
        }
        HologramData hologramData = hologram.getData();
        if (hologramData instanceof TextHologramData) {
            TextHologramData textData = (TextHologramData)hologramData;
            textData.setText(this.getLines(chest));
            hologram.queueUpdate();
        }
        Logger.debug("Updated hologram for chest " + chest.getId());
    }

    @Override
    public void updateHologramLocation(Chest chest) {
        Hologram hologram = this.manager.getHologram(H_PREFIX + chest.getId()).orElse(null);
        if (hologram == null) {
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
            hologram.getData().setLocation(new Location(Bukkit.getWorld((String)loc.getLeftLocation().world), x + 0.5, (double)loc.getLeftLocation().y + 1.35, z + 0.5));
        } else {
            hologram.getData().setLocation(new Location(Bukkit.getWorld((String)loc.getLeftLocation().world), (double)loc.getLeftLocation().x + 0.5, (double)loc.getLeftLocation().y + 1.35, (double)loc.getLeftLocation().z + 0.5));
        }
        hologram.queueUpdate();
        Logger.debug("Updated hologram location for chest " + chest.getId());
    }

    @Override
    public void tickHologram(Chest chest, String interval) {
        Hologram hologram = this.manager.getHologram(H_PREFIX + chest.getId()).orElse(null);
        if (hologram == null) {
            return;
        }
        HologramData hologramData = hologram.getData();
        if (hologramData instanceof TextHologramData) {
            TextHologramData textData = (TextHologramData)hologramData;
            List text = textData.getText();
            text.set(this.tickLine, this.lines.get(this.tickLine).replace("%next-interval%", interval));
            textData.setText(text);
            hologram.queueUpdate();
        }
    }

    @Override
    public void removeHologram(Chest chest) {
        Hologram hologram = this.manager.getHologram(H_PREFIX + chest.getId()).orElse(null);
        if (hologram == null) {
            return;
        }
        this.manager.removeHologram(hologram);
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

