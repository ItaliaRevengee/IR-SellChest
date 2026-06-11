/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatMessageType
 *  net.md_5.bungee.api.chat.BaseComponent
 *  net.md_5.bungee.api.chat.TextComponent
 *  org.bukkit.Bukkit
 *  org.bukkit.boss.BarColor
 *  org.bukkit.boss.BarFlag
 *  org.bukkit.boss.BarStyle
 *  org.bukkit.boss.BossBar
 *  org.bukkit.entity.Player
 */
package com.italiarevenge.iRSellChest.util;

import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.util.Logger;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public enum ChestConfirmation {
    NONE,
    ACTION_BAR,
    BOSS_BAR;


    public void playEffect(Player p) {
        if (this == BOSS_BAR) {
            BarColor color;
            try {
                color = BarColor.valueOf((String)Config.get().getString("chest-confirmation-boss-bar-color"));
            }
            catch (IllegalArgumentException e) {
                color = BarColor.GREEN;
                Logger.warn("Failed to find a boss bar color for: '" + Config.get().getString("chest-confirmation-boss-bar-color") + "'");
            }
            BossBar bar = Bukkit.createBossBar((String)Lang.PLACED_SELL_CHESTS_BOSS_BAR.get(), (BarColor)color, (BarStyle)BarStyle.SEGMENTED_10, (BarFlag[])new BarFlag[0]);
            bar.setProgress(Math.min((double)(AutoSellChests.getInstance().getManager().getOwnedChests(p) * 100 / AutoSellChests.getInstance().getManager().getMaxSell(p)) * 0.01, 1.0));
            bar.addPlayer(p);
            AutoSellChests.getInstance().runTaskLater(() -> bar.removePlayer(p), 100L);
        } else if (this == ACTION_BAR) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent)new TextComponent(Lang.PLACED_SELL_CHESTS_ACTION_BAR.get().replace("%amount%", String.valueOf(AutoSellChests.getInstance().getManager().getOwnedChests(p))).replace("%limit%", p.hasPermission("autosellchests.maxchests.override") ? Lang.PLACED_SELL_CHESTS_ACTION_BAR_MAX.get() : String.valueOf(AutoSellChests.getInstance().getManager().getMaxSell(p)))));
        }
    }
}

