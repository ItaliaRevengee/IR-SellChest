/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.ExplosionResult
 *  org.bukkit.Material
 *  org.bukkit.block.Block
 *  org.bukkit.block.Chest
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.BlockExplodeEvent
 *  org.bukkit.event.entity.EntityExplodeEvent
 */
package com.italiarevenge.iRSellChest.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.objects.ChestLocation;
import com.italiarevenge.iRSellChest.util.Logger;
import org.bukkit.ExplosionResult;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockListener
implements Listener {
    private final AutoSellChests plugin;

    public BlockListener(AutoSellChests plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled() || this.plugin.version == 121 && (event.getExplosionResult() == ExplosionResult.TRIGGER_BLOCK || event.getExplosionResult() == ExplosionResult.KEEP)) {
            return;
        }
        this.handleBlockExploison(event.blockList());
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        this.handleBlockExploison(event.blockList());
    }

    private void handleBlockExploison(List<Block> affected) {
        ArrayList<Block> toRemove = new ArrayList<Block>();
        for (Block block : affected) {
            Chest chest;
            if (block == null || block.getType() != Material.CHEST || (chest = this.plugin.getManager().getChestByLocation(block.getLocation())) == null) continue;
            toRemove.add(block);
            this.plugin.getManager().removeChest(new ChestLocation(block.getLocation()));
            block.getWorld().dropItemNaturally(block.getLocation(), this.plugin.getManager().getChest(chest, 1));
            if (this.plugin.version != 116) {
                Arrays.stream(((org.bukkit.block.Chest)block.getState()).getBlockInventory().getContents()).forEach(item -> {
                    if (item != null && item.getType() != Material.AIR) {
                        block.getWorld().dropItemNaturally(block.getLocation(), item);
                    }
                });
            }
            block.setType(Material.AIR);
            Logger.debug("SellChest at location 'World '" + chest.getLocation().getLeftLocation().world + "', x" + chest.getLocation().getLeftLocation().x + ", y" + chest.getLocation().getLeftLocation().y + ", z" + chest.getLocation().getLeftLocation().z + "' exploded");
        }
        affected.removeAll(toRemove);
    }
}

