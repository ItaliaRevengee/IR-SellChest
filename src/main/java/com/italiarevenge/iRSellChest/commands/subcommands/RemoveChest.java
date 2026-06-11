/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.block.Block
 *  org.bukkit.block.Chest
 *  org.bukkit.entity.Player
 */
package com.italiarevenge.iRSellChest.commands.subcommands;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.commands.SubCommad;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class RemoveChest
implements SubCommad {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Remove the chest you are looking at or with the chests id which you can get by using /asc view <player>";
    }

    @Override
    public String getSyntax() {
        return "/asc remove [id]";
    }

    @Override
    public void perform(Object logger, String[] args) {
        if (args.length == 1) {
            if (logger instanceof Player) {
                Chest chest;
                Player player = (Player)logger;
                Block block = player.getTargetBlockExact(10);
                if (block.getType() == Material.CHEST && (chest = AutoSellChests.getInstance().getManager().getChestByLocation(block.getLocation())) != null) {
                    AutoSellChests.getInstance().getManager().removeChest(chest);
                    this.breakNaturally(chest, chest.getLocation().getLeftLocation().toLoc());
                    Logger.sendPlayerMessage(player, String.valueOf(ChatColor.GREEN) + "Successfully broken chest from " + Bukkit.getOfflinePlayer((UUID)chest.getOwner()).getName());
                    return;
                }
                Logger.sendPlayerMessage(player, String.valueOf(ChatColor.RED) + "No AutoSellChest found at this location");
            } else {
                Logger.sendMessage(logger, this.getSyntax());
            }
        } else {
            try {
                int id = Integer.parseInt(args[1]);
                Chest chest = AutoSellChests.getInstance().getManager().getChestByID(id);
                if (chest != null) {
                    Logger.sendMessage(logger, String.valueOf(ChatColor.GREEN) + "Successfully broken chest from " + Bukkit.getOfflinePlayer((UUID)chest.getOwner()).getName());
                    AutoSellChests.getInstance().getManager().removeChest(chest);
                    this.breakNaturally(chest, chest.getLocation().getLeftLocation().toLoc());
                } else {
                    Logger.sendMessage(logger, String.valueOf(ChatColor.RED) + "No sell chest found with ID " + args[1]);
                }
            }
            catch (NumberFormatException ex) {
                Logger.sendMessage(logger, String.valueOf(ChatColor.RED) + "Not a valid id");
            }
        }
    }

    private void breakNaturally(Chest chest, Location loc) {
        loc.add(0.5, 0.5, 0.5);
        Arrays.stream(((org.bukkit.block.Chest)chest.getLocation().getLeftLocation().toLoc().getBlock().getState()).getInventory().getContents()).forEach(item -> {
            if (item != null && item.getType() != Material.AIR) {
                loc.getWorld().dropItemNaturally(loc, item);
            }
        });
        if (AutoSellChests.getInstance().version == 116) {
            ((org.bukkit.block.Chest)loc.getBlock().getState()).getBlockInventory().clear();
        }
        loc.getBlock().setType(Material.AIR);
        if (chest.getLocation().isDoubleChest()) {
            chest.getLocation().getRightLocation().toLoc().getBlock().setType(Material.AIR);
        }
        loc.getWorld().dropItemNaturally(loc, AutoSellChests.getInstance().getManager().getChest(chest, chest.getLocation().isDoubleChest() ? 2 : 1));
    }

    @Override
    public List<String> getTabCompletion(String[] args) {
        return null;
    }
}

