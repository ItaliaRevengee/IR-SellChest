/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.world.ChunkLoadEvent
 *  org.bukkit.event.world.ChunkUnloadEvent
 */
package com.italiarevenge.iRSellChest.events;

import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.objects.ChunkLoc;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener
implements Listener {
    private final AutoSellChests plugin;

    public ChunkListener(AutoSellChests plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        this.plugin.getManager().loadChests(new ChunkLoc(e.getChunk()));
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        this.plugin.getManager().unloadChests(new ChunkLoc(e.getChunk()));
    }
}

