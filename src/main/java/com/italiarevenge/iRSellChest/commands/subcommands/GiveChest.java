/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package com.italiarevenge.iRSellChest.commands.subcommands;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.commands.SubCommad;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveChest
implements SubCommad {
    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getDescription() {
        return "&7Gives a #CDCDCDAuto#FF7070Sell#CDCDCDChest &7to the player specified";
    }

    @Override
    public String getSyntax() {
        return "/asc give [player] [quantity] [SellMultiplier]";
    }

    @Override
    public void perform(Object logger, String[] args) {
        if (args.length < 1) {
            Logger.sendMessage(logger, this.getSyntax());
            return;
        }
        if (args.length == 1) {
            if (logger instanceof Player) {
                Player player = (Player)logger;
                this.giveQuantity(logger, player, 1);
            } else {
                Logger.sendMessage(logger, "You need to specify a player");
            }
        } else {
            Player p;
            if (args.length == 2 && logger instanceof Player) {
                Player player = (Player)logger;
                try {
                    int qty = Integer.parseInt(args[1]);
                    this.giveQuantity(logger, player, qty);
                    return;
                }
                catch (NumberFormatException ignored) {
                    this.giveQuantity(logger, player, 1);
                }
            }
            if ((p = Bukkit.getPlayer((String)args[1])) != null) {
                if (args.length == 3) {
                    try {
                        int qty = Integer.parseInt(args[2]);
                        this.giveQuantity(logger, p, qty);
                    }
                    catch (NumberFormatException ex) {
                        Logger.sendMessage(logger, "Not a valid amount");
                    }
                } else if (args.length == 4) {
                    try {
                        int qty = Integer.parseInt(args[2]);
                        try {
                            double multiplier = Double.parseDouble(args[3]);
                            this.giveQuantity(logger, p, qty, multiplier);
                        }
                        catch (NumberFormatException ex) {
                            Logger.sendMessage(logger, "Not a valid sell multiplier");
                        }
                    }
                    catch (NumberFormatException ex) {
                        Logger.sendMessage(logger, "Not a valid amount");
                    }
                }
            } else {
                Logger.sendMessage(logger, "Could not find a online player with name " + args[1]);
            }
        }
    }

    private void giveQuantity(Object logger, Player player, int qty) {
        if (player.getInventory().firstEmpty() == -1) {
            Logger.sendMessage(logger, Lang.NOT_ENOUGH_INVENTORY_SPACE.get());
            return;
        }
        player.getInventory().addItem(new ItemStack[]{AutoSellChests.getInstance().getManager().getChest(qty)});
        Logger.sendPlayerMessage(player, Lang.PLAYER_SELL_CHEST_GIVEN.get().replace("%amount%", String.valueOf(qty)));
        Logger.info(Lang.SELL_CHEST_GIVEN_LOG.get().replace("%player_name%", player.getName()).replace("%amount%", String.valueOf(qty)));
    }

    private void giveQuantity(Object logger, Player player, int qty, double multiplier) {
        if (player.getInventory().firstEmpty() == -1) {
            Logger.sendMessage(logger, Lang.NOT_ENOUGH_INVENTORY_SPACE.get());
            return;
        }
        player.getInventory().addItem(new ItemStack[]{AutoSellChests.getInstance().getManager().getChest(qty)});
        Logger.sendPlayerMessage(player, Lang.PLAYER_SELL_CHEST_GIVEN.get().replace("%amount%", String.valueOf(qty)));
        Logger.info(Lang.SELL_CHEST_MULTIPLIER_GIVEN_LOG.get().replace("%player_name%", player.getName()).replace("%amount%", String.valueOf(qty)).replace("%multiplier%", String.valueOf(multiplier)));
    }

    @Override
    public List<String> getTabCompletion(String[] args) {
        switch (args.length) {
            case 2: {
                return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
            }
            case 3: {
                return Arrays.asList("1", "2", "3", "5", "10");
            }
        }
        return null;
    }
}

