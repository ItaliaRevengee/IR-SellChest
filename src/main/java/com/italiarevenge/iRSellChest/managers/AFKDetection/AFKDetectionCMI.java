/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.Zrips.CMI.CMI
 *  com.Zrips.CMI.Modules.Afk.AfkManager
 */
package com.italiarevenge.iRSellChest.managers.AFKDetection;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Afk.AfkManager;
import java.util.UUID;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.managers.AFKManager;
import com.italiarevenge.iRSellChest.util.Logger;

public class AFKDetectionCMI
implements AFKManager {
    private final AfkManager manager = CMI.getInstance().getAfkManager();

    public AFKDetectionCMI(AutoSellChests plugin) {
        Logger.info("Enabled AFK detection provider 'CMI'");
    }

    @Override
    public boolean isAFK(UUID uuid) {
        return this.manager.isAfk(uuid);
    }
}

