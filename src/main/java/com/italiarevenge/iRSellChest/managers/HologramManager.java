/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.plugin.PluginManager
 */
package com.italiarevenge.iRSellChest.managers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.managers.HologramProvider;
import com.italiarevenge.iRSellChest.managers.holograms.CMIHologramHook;
import com.italiarevenge.iRSellChest.managers.holograms.DecentHologramHook;
import com.italiarevenge.iRSellChest.managers.holograms.FakeHologramHook;
import com.italiarevenge.iRSellChest.managers.holograms.FancyHologramHook;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.util.Logger;
import com.italiarevenge.iRSellChest.util.TimeUtils;
import org.bukkit.plugin.PluginManager;

public class HologramManager {
    private final AutoSellChests plugin;
    private HologramProvider provider;

    public HologramManager(AutoSellChests plugin) {
        this.plugin = plugin;
        if (Config.get().getBoolean("chest-holograms.enabled")) {
            PluginManager pm = plugin.getServer().getPluginManager();
            if (pm.getPlugin("DecentHolograms") != null) {
                this.provider = new DecentHologramHook();
            } else if (pm.getPlugin("FancyHolograms") != null) {
                this.provider = new FancyHologramHook();
            } else if (pm.getPlugin("CMI") != null) {
                this.provider = new CMIHologramHook(plugin);
            }
        }
        if (this.provider == null) {
            Logger.info("Failed to find a supported hologram provider, disabling holograms...");
            this.provider = new FakeHologramHook();
        } else {
            Logger.info("Enabled holograms hook: " + (this.provider instanceof DecentHologramHook ? "DecentHolograms" : (this.provider instanceof FancyHologramHook ? "FancyHolograms" : "CMI")));
        }
        if (!(this.provider instanceof FakeHologramHook)) {
            this.tickHolograms();
        }
    }

    public boolean isEnabled() {
        return !(this.provider instanceof FakeHologramHook);
    }

    public void loadHologram(Chest chest) {
        this.provider.loadHologram(chest);
    }

    public void updateHologram(Chest chest) {
        this.provider.updateHologram(chest);
    }

    public void updateHologramLocation(Chest chest) {
        this.provider.updateHologramLocation(chest);
    }

    public void removeHologram(Chest chest) {
        this.provider.removeHologram(chest);
    }

    private void tickHolograms() {
        long millis = 20L;
        try {
            millis = TimeUtils.getTime(Config.get().getString("chest-holograms.update-interval", "1s")) / 50L;
        }
        catch (ParseException e) {
            Logger.warn("Failed to parse chest hologram update interval for " + Config.get().getString("chest-holograms.update-interval") + " with reason: " + e.getMessage());
            e.printStackTrace();
        }
        this.plugin.runTaskAsyncTimer(() -> {
            // Snapshot avoids ConcurrentModificationException when the main thread adds/removes chests
            List<Chest> snapshot = new ArrayList<>(this.plugin.getManager().getLoadedChests().values());
            for (Chest c : snapshot) {
                if (!c.isLoaded() || !c.isHologram()) continue;
                this.provider.tickHologram(c, this.getNextInterval(c));
            }
        }, 0L, millis);
    }

    private String getNextInterval(Chest chest) {
        return TimeUtils.getReadableTime(chest.getNextInterval() - (System.currentTimeMillis() - 1000L));
    }
}

