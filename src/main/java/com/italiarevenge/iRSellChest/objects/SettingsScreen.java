/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.Inventory
 */
package com.italiarevenge.iRSellChest.objects;

import java.util.Collections;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.objects.ChestInventory;
import com.italiarevenge.iRSellChest.util.SimpleInventoryBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SettingsScreen
extends ChestInventory {
    private Inventory inv;
    private final Chest chest;
    private final Location selectedChest;
    private boolean update;

    public SettingsScreen(Chest chest, Location selectedChest) {
        this.selectedChest = selectedChest;
        this.chest = chest;
        this.init();
    }

    private void init() {
        SimpleInventoryBuilder builder = AutoSellChests.getInstance().getInventoryManager().getSettingsInv();
        builder.init(this);
        builder.replace("logging-item", Collections.singletonMap("%value%", String.valueOf(this.chest.isLogging())));
        builder.replace("rename-item", Collections.singletonMap("%chest-name%", String.valueOf(this.chest.getName())));
        if (AutoSellChests.getInstance().getHologramManager().isEnabled()) {
            builder.enableItem("hologram-item");
            builder.replace("hologram-item", Collections.singletonMap("%value%", String.valueOf(this.chest.isHologram())));
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

