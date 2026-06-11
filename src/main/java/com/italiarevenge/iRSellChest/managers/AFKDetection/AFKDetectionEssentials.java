/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.earth2me.essentials.Essentials
 */
package com.italiarevenge.iRSellChest.managers.AFKDetection;

import com.earth2me.essentials.Essentials;
import java.util.UUID;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.managers.AFKManager;
import com.italiarevenge.iRSellChest.util.Logger;

public class AFKDetectionEssentials
implements AFKManager {
    private final Essentials ess;

    public AFKDetectionEssentials(AutoSellChests plugin) {
        this.ess = (Essentials)plugin.getServer().getPluginManager().getPlugin("Essentials");
        Logger.info("Enabled AFK detection provider 'Essentials'");
    }

    @Override
    public boolean isAFK(UUID uuid) {
        return this.ess.getUser(uuid).isAfk();
    }
}

