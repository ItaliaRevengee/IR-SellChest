/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.scheduler.BukkitTask
 */
package com.italiarevenge.iRSellChest.util.scheduler.tasks;

import com.italiarevenge.iRSellChest.util.scheduler.Task;

public class BukkitTask
implements Task {
    private final org.bukkit.scheduler.BukkitTask task;

    public BukkitTask(org.bukkit.scheduler.BukkitTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        this.task.cancel();
    }
}

