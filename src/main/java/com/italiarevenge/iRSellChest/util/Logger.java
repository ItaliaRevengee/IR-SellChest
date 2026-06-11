/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.command.ConsoleCommandSender
 *  org.bukkit.entity.Player
 */
package com.italiarevenge.iRSellChest.util;

import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.files.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public final class Logger {
    private static AutoSellChests plugin;
    private static boolean debug;
    private static String prefix;
    private static ConsoleCommandSender logger;

    public Logger(AutoSellChests plugin) {
        Logger.plugin = plugin;
        debug = Config.get().getBoolean("debug");
        prefix = "\u00a78[\u00a76Auto\u00a74Sell\u00a76Chests\u00a78]\u00a7r";
        logger = plugin.getServer().getConsoleSender();
    }

    public static void sendPlayerMessage(Player p, String s) {
        p.sendMessage(Lang.PLUGIN_PREFIX.get() + String.valueOf(ChatColor.RESET) + " " + s);
    }

    public static void sendMessage(Object logger, String s) {
        if (logger instanceof Player) {
            Logger.sendPlayerMessage((Player)logger, s);
        } else if (logger instanceof ConsoleCommandSender) {
            ((ConsoleCommandSender)logger).sendMessage(s);
        }
    }

    public static void info(String s) {
        logger.sendMessage(prefix + " \u00a78[\u00a77INFO\u00a78]\u00a7r: " + s);
    }

    public static void warn(String s) {
        logger.sendMessage(prefix + " \u00a78[\u00a7cWARN\u00a78]\u00a7r: " + s);
    }

    public static void debug(String s) {
        if (debug) {
            logger.sendMessage(prefix + "\u00a78[\u00a76DEBUG\u00a78]\u00a7r: " + s);
        }
    }
}

