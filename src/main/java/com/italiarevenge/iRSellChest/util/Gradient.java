/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatColor
 */
package com.italiarevenge.iRSellChest.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.italiarevenge.iRSellChest.util.Logger;
import net.md_5.bungee.api.ChatColor;

public class Gradient {
    private int c;
    private final String original;
    private final String start;
    private final String end;
    private final List<ChatColor> colors = new ArrayList<ChatColor>();
    private final Pattern rbgPattern = Pattern.compile("#[a-fA-F0-9]{6}");

    public Gradient(String startGradient, String endGradient, String original) {
        Color c2;
        int start = original.indexOf(startGradient);
        int end = original.indexOf(endGradient) + endGradient.length();
        this.start = original.substring(0, start);
        this.end = original.substring(end);
        this.original = original.substring(start, end).replace(startGradient, "").replace(endGradient, "");
        Color c1 = this.getColor(startGradient);
        if (c1 == null) {
            c1 = Color.decode("#D4AF37");
        }
        if ((c2 = this.getColor(endGradient)) == null) {
            c2 = Color.decode("#FADA6F");
        }
        for (int i = 0; i < this.original.length(); ++i) {
            float ratio = (float)i / (float)Math.ceil((double)this.original.length() * 1.25);
            int r = (int)((float)c2.getRed() * ratio + (float)c1.getRed() * (1.0f - ratio));
            int g = (int)((float)c2.getGreen() * ratio + (float)c1.getGreen() * (1.0f - ratio));
            int b = (int)((float)c2.getBlue() * ratio + (float)c1.getBlue() * (1.0f - ratio));
            this.colors.add(ChatColor.of((Color)new Color(r, g, b)));
        }
    }

    private Color getColor(String gradientPattern) {
        Matcher m1 = this.rbgPattern.matcher(gradientPattern);
        if (m1.find()) {
            try {
                return Color.decode(gradientPattern.substring(m1.start(), m1.end()));
            }
            catch (NumberFormatException e) {
                Logger.warn("Invalid rgb format for '" + gradientPattern + "', using default gradient colors...");
            }
        } else {
            Logger.warn("Invalid rgb format for '" + gradientPattern + "', using default gradient colors...");
        }
        return null;
    }

    public ChatColor getColor(int i) {
        return this.colors.get(i);
    }

    public String get() {
        int i = 0;
        StringBuilder builder = new StringBuilder();
        builder.append(this.start);
        for (char c : this.original.toCharArray()) {
            builder.append(this.getColor(i)).append(ChatColor.BOLD).append(c);
            ++i;
        }
        builder.append(this.end);
        return builder.toString();
    }
}

