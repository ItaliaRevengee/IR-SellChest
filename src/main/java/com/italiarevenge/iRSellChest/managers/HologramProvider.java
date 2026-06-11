/*
 * Decompiled with CFR 0.152.
 */
package com.italiarevenge.iRSellChest.managers;

import com.italiarevenge.iRSellChest.objects.Chest;

public interface HologramProvider {
    public void loadHologram(Chest var1);

    public void unloadHologram(Chest var1);

    public void updateHologram(Chest var1);

    public void updateHologramLocation(Chest var1);

    public void tickHologram(Chest var1, String var2);

    public void removeHologram(Chest var1);
}

