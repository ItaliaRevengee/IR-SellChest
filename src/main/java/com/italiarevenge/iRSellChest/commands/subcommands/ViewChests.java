/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.HumanEntity
 */
package com.italiarevenge.iRSellChest.commands.subcommands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.commands.SubCommad;
import com.italiarevenge.iRSellChest.managers.UpgradeManager;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.util.Logger;
import com.italiarevenge.iRSellChest.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;

public class ViewChests
implements SubCommad {
    @Override
    public String getName() {
        return "view";
    }

    @Override
    public String getDescription() {
        return "Get information about the chest(s) a player has";
    }

    @Override
    public String getSyntax() {
        return "/asc view <player>";
    }

    @Override
    public void perform(Object logger, String[] args) {
        if (args.length > 1) {
            ArrayList<Chest> chests = AutoSellChests.getInstance().getManager().getChestsByPlayer(Bukkit.getOfflinePlayer((String)args[1]).getUniqueId());
            if (!chests.isEmpty()) {
                Logger.sendMessage(logger, ChatColor.translateAlternateColorCodes((char)'&', (String)("&aFound &7%qty% &aAutoSellChests for " + args[1] + "(" + String.valueOf(Bukkit.getOfflinePlayer((String)args[1]).getUniqueId()) + ")")).replace("%qty%", String.valueOf(chests.size())));
                for (Chest chest : chests) {
                    Logger.sendMessage(logger, ChatColor.translateAlternateColorCodes((char)'&', (String)("&eID: &c" + chest.getId() + " &7| &eLocation: &cWorld '" + chest.getLocation().getLeftLocation().world + "', x" + chest.getLocation().getLeftLocation().z + ", y" + chest.getLocation().getLeftLocation().y + ", z" + chest.getLocation().getLeftLocation().z + " &7| &eTotalProfit: &c" + chest.getIncome("\u00a7c") + " &7| &eTotalItemsSold: &c" + chest.getItemsSold() + " &7| &eDoubleChest: &c" + chest.getLocation().isDoubleChest() + (String)(UpgradeManager.intervalUpgrades ? " &7| &eInterval: &c" + UpgradeManager.getIntervalUpgrade(chest.getIntervalUpgrade()).getName() + "&f(" + TimeUtils.getReadableTime(chest.getInterval()) + ")" : "") + (String)(UpgradeManager.multiplierUpgrades ? " &7| &eMultiplier: &c" + UpgradeManager.getMultiplierUpgrade(chest.getMultiplierUpgrade()).getName() + "&f(x" + chest.getMultiplier() + ")" : ""))));
                }
            } else {
                Logger.sendMessage(logger, String.valueOf(ChatColor.RED) + "This player has no placed AutoSellChests");
            }
        } else {
            Logger.sendMessage(logger, this.getSyntax());
        }
    }

    @Override
    public List<String> getTabCompletion(String[] args) {
        switch (args.length) {
            case 2: {
                return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
            }
        }
        return null;
    }
}

