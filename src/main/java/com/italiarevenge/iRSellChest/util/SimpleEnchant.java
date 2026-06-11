/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.NamespacedKey
 *  org.bukkit.enchantments.Enchantment
 */
package com.italiarevenge.iRSellChest.util;

import java.util.Locale;
import com.italiarevenge.iRSellChest.util.Version;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

public enum SimpleEnchant {
    UNBREAKING("DURABILITY");

    private static boolean components;
    private final String[] legacy;

    private SimpleEnchant(String ... legacy) {
        this.legacy = legacy;
    }

    public String getVanillaName() {
        return components || this.legacy.length == 0 ? this.name() : this.legacy[0];
    }

    public Enchantment get() {
        return Enchantment.getByKey((NamespacedKey)NamespacedKey.minecraft((String)this.getVanillaName().toLowerCase(Locale.ENGLISH)));
    }

    static {
        try {
            Version ver = new Version(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].replace("_", ".").replace("v", "").replace("R", ""));
            components = ver.isGreater(new Version("1.20.4"));
        }
        catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            Version ver = new Version(Bukkit.getBukkitVersion().split("-")[0]);
            components = ver.isGreater(new Version("1.20.4"));
        }
    }
}

