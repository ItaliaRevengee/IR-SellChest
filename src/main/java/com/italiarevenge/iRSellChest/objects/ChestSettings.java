/*
 * Decompiled with CFR 0.152.
 */
package com.italiarevenge.iRSellChest.objects;

import java.util.Objects;
import com.italiarevenge.iRSellChest.util.Logger;

public class ChestSettings {
    public final boolean logging;
    public final int interval;
    public final int multiplier;
    public final boolean hologram;

    public ChestSettings(String settings) {
        String[] parts = settings.split("\\|");
        this.logging = parts[0].equals("1");
        this.interval = this.getInt(settings, 1);
        this.multiplier = this.getInt(settings, 2);
        this.hologram = this.getInt(settings, 3, 1) == 1;
    }

    public ChestSettings(boolean logging, int interval, int multiplier, boolean hologram) {
        this.logging = logging;
        this.interval = interval;
        this.multiplier = multiplier;
        this.hologram = hologram;
    }

    public ChestSettings() {
        this.logging = true;
        this.interval = 0;
        this.multiplier = 0;
        this.hologram = true;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.logging ? "1" : "0").append("|");
        builder.append(this.interval).append("|");
        builder.append(this.multiplier).append("|");
        builder.append(this.hologram ? "1" : Integer.valueOf(0)).append("|");
        return builder.toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ChestSettings) {
            ChestSettings settings = (ChestSettings)object;
            return this.interval == settings.interval && this.multiplier == settings.multiplier;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.interval, this.multiplier);
    }

    private int getInt(String settings, int path) {
        return this.getInt(settings, path, 0);
    }

    private int getInt(String settings, int path, int def) {
        try {
            return Integer.parseInt(settings.split("\\|")[path]);
        }
        catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            Logger.warn("Failed to load upgrade level for '" + settings + "' for chest, using default...");
            return def;
        }
    }
}

