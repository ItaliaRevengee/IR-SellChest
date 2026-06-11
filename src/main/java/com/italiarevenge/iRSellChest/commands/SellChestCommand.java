/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ChatColor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.ConsoleCommandSender
 *  org.bukkit.command.defaults.BukkitCommand
 *  org.bukkit.entity.Player
 *  org.bukkit.util.StringUtil
 */
package com.italiarevenge.iRSellChest.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.commands.SubCommad;
import com.italiarevenge.iRSellChest.commands.subcommands.GiveChest;
import com.italiarevenge.iRSellChest.commands.subcommands.Reload;
import com.italiarevenge.iRSellChest.commands.subcommands.RemoveChest;
import com.italiarevenge.iRSellChest.commands.subcommands.ViewChests;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.util.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class SellChestCommand
extends BukkitCommand {
    private final ArrayList<SubCommad> subCommads = new ArrayList();
    private final AutoSellChests plugin;

    public SellChestCommand(AutoSellChests plugin) {
        super("autosellchests");
        this.description = "The main command of AutoSellChests";
        this.setPermission("autosellchests.use");
        this.usageMessage = "/autosellchests <subcommand> <args>";
        this.setAliases(Collections.singletonList("asc"));
        this.subCommads.add(new GiveChest());
        this.subCommads.add(new ViewChests());
        this.subCommads.add(new RemoveChest());
        this.subCommads.add(new Reload());
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (args.length > 0) {
                for (SubCommad subCommad : this.subCommads) {
                    if (!args[0].equalsIgnoreCase(subCommad.getName())) continue;
                    if (player.hasPermission("autosellchests." + subCommad.getName())) {
                        subCommad.perform(sender, args);
                    } else {
                        Logger.sendMessage(sender, Lang.NO_PERMISSIONS.get());
                    }
                    return true;
                }
            }
            if (this.hasPermission(player)) {
                this.sendAllSyntaxes(sender);
            } else {
                player.sendMessage(Lang.NO_PERMISSIONS.get());
            }
        } else if (sender instanceof ConsoleCommandSender) {
            if (args.length > 0) {
                for (SubCommad subCommad : this.subCommads) {
                    if (!args[0].toLowerCase().equalsIgnoreCase(subCommad.getName())) continue;
                    subCommad.perform(sender, args);
                    return true;
                }
            }
            this.sendAllSyntaxes(sender);
        }
        return true;
    }

    private boolean hasPermission(Player p) {
        return p.hasPermission("autosellchests.give") || p.hasPermission("autosellchests.view") || p.hasPermission("autosellchests.reload") || p.hasPermission("autosellchests.remove");
    }

    public List<String> tabComplete(CommandSender commandSender, String s, String[] args) {
        ArrayList<String> tabCompletions = new ArrayList<String>();
        if (args.length == 1) {
            for (SubCommad subCommad : this.subCommads) {
                tabCompletions.add(subCommad.getName());
            }
            if (!args[0].isEmpty()) {
                ArrayList<String> completions = new ArrayList<String>();
                StringUtil.copyPartialMatches((String)args[0], tabCompletions, completions);
                Collections.sort(completions);
                return completions;
            }
            return tabCompletions;
        }
        if (args.length > 1) {
            for (SubCommad subCommad : this.subCommads) {
                if (!args[0].equalsIgnoreCase(subCommad.getName())) continue;
                return subCommad.getTabCompletion(args);
            }
        }
        return null;
    }

    private void sendAllSyntaxes(Object logger) {
        Logger.sendMessage(logger, String.valueOf(ChatColor.DARK_GREEN) + "----------------------------------------");
        for (SubCommad subCommad : this.subCommads) {
            Logger.sendMessage(logger, " ");
            Logger.sendMessage(logger, String.valueOf(ChatColor.DARK_GREEN) + "- " + String.valueOf(ChatColor.GREEN) + subCommad.getSyntax() + " - " + subCommad.getDescription());
            Logger.sendMessage(logger, " ");
        }
        Logger.sendMessage(logger, String.valueOf(ChatColor.DARK_GREEN) + "----------------------------------------");
    }
}

