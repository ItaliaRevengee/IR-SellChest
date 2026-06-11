/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemStack
 */
package com.italiarevenge.iRSellChest.objects;

import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.managers.UpgradeManager;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.objects.ChestInventory;
import com.italiarevenge.iRSellChest.util.SimpleInventoryBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class UpgradeScreen
extends ChestInventory {
    private Inventory inv;
    private final Chest chest;
    private final Location selectedChest;
    private boolean update;

    public UpgradeScreen(Chest chest, Location selectedChest) {
        this.selectedChest = selectedChest;
        this.chest = chest;
        this.init();
    }

    private void init() {
        ItemStack upgradeItem;
        SimpleInventoryBuilder builder = AutoSellChests.getInstance().getInventoryManager().getUpgradeInv();
        builder.init(this);
        if (UpgradeManager.intervalUpgrades) {
            upgradeItem = UpgradeManager.getIntervalUpgrade(this.chest.getIntervalUpgrade()).getUpgradeItem(this.chest.isDoubleChest());
            builder.insertItem("interval-upgrade-item", upgradeItem);
        }
        if (UpgradeManager.multiplierUpgrades) {
            upgradeItem = UpgradeManager.getMultiplierUpgrade(this.chest.getMultiplierUpgrade()).getUpgradeItem(this.chest.isDoubleChest());
            builder.insertItem("multiplier-upgrade-item", upgradeItem);
        }
        this.inv = builder.build();
    }

    @Override
    public Chest getChest() {
        return this.chest;
    }

    @Override
    public Location getSelectedChest() {
        return this.selectedChest;
    }

    @Override
    public boolean isUpdatingInventory() {
        return this.update;
    }

    public void open(Player p) {
        p.openInventory(this.inv);
    }

    public void update() {
        this.update = true;
        AutoSellChests.getInstance().runTaskLater(() -> {
            this.update = false;
        }, 1L);
    }

    public void updateInventory(Player p) {
        this.update();
        this.inv.clear();
        this.init();
        this.open(p);
    }
}

