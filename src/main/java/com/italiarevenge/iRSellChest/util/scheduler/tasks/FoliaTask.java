/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.threadedregions.scheduler.ScheduledTask
 */
package com.italiarevenge.iRSellChest.util.scheduler.tasks;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import com.italiarevenge.iRSellChest.util.scheduler.Task;

public class FoliaTask
implements Task {
    private final ScheduledTask task;

    public FoliaTask(ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        this.task.cancel();
    }
}

