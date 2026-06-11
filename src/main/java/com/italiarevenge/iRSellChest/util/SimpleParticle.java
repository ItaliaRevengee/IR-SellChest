/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Particle
 */
package com.italiarevenge.iRSellChest.util;

import com.italiarevenge.iRSellChest.util.Version;
import org.bukkit.Bukkit;
import org.bukkit.Particle;

public enum SimpleParticle {
    WITCH("SPELL_WITCH"),
    DUST("REDSTONE"),
    CLOUD(new String[0]);

    private static boolean components;
    private final String[] legacy;

    private SimpleParticle(String ... legacy) {
        this.legacy = legacy;
    }

    public Particle get() {
        return Particle.valueOf((String)(components || this.legacy.length == 0 ? this.name() : this.legacy[0]));
    }

    static {
        try {
            Version ver = new Version(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].replace("_", ".").replace("v", "").replace("R", ""));
            components = ver.isGreater(new Version("1.20.3"));
        }
        catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            Version ver = new Version(Bukkit.getBukkitVersion().split("-")[0]);
            components = ver.isGreater(new Version("1.20.4"));
        }
    }
}

