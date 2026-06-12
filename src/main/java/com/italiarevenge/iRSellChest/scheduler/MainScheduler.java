package com.italiarevenge.iRSellChest.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.hooks.IRShopHook;
import com.italiarevenge.iRSellChest.managers.UpgradeManager;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.objects.ChestLocation;
import com.italiarevenge.iRSellChest.objects.IntervalLogger;
import com.italiarevenge.iRSellChest.util.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainScheduler {

    private final AutoSellChests plugin;
    private final ScheduledExecutorService SCHEDULER_THREAD = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("ASC_SCHEDULER_THREAD").build());
    private final SchedulerQueue queue;
    private final IntervalLogger logger;
    private boolean running = true;
    private final boolean onlineOwner;
    private final boolean intervalLogging;
    private final boolean afkDetection;

    public MainScheduler(AutoSellChests plugin) {
        this.plugin = plugin;
        this.queue = new SchedulerQueue();
        this.onlineOwner = Config.get().getBoolean("online-chest-owner", true);
        this.intervalLogging = Config.get().getBoolean("interval-logs.enable");
        this.afkDetection = plugin.getAFKManager() != null;
        this.logger = Config.get().getString("interval-logs.interval", "").isEmpty()
                ? null : new IntervalLogger(this.SCHEDULER_THREAD);
        Logger.info("Starting sell interval...");
        this.SCHEDULER_THREAD.scheduleAtFixedRate(this::processChests, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    private void processChests() {
        Chest nextChest = this.queue.peek();
        if (nextChest == null) return;
        while (this.running && nextChest.getNextInterval() - System.currentTimeMillis() <= 50L) {
            Chest chest = this.queue.getNextAndUpdate();
            this.plugin.runTask(chest, () -> this.sellContents(chest));
            nextChest = this.queue.peek();
        }
    }

    private void sellContents(Chest chest) {
        try {
            if (!chest.isLoaded()) {
                Logger.debug("Tried to process chest " + chest.getId() + " but is in unloaded chunk, skipping...");
                return;
            }

            UUID ownerUUID = chest.getOwner();
            // getPlayer() returns the live Player without any disk I/O; getOfflinePlayer() can hit disk
            Player onlinePlayer = Bukkit.getPlayer(ownerUUID);

            if (this.onlineOwner && onlinePlayer == null) {
                Logger.debug("Owner from chest " + chest.getId() + " is not online, skipping...");
                return;
            }
            if (this.afkDetection && this.plugin.getAFKManager().isAFK(ownerUUID)) {
                Logger.debug("Owner from chest " + chest.getId() + " is afk, skipping...");
                return;
            }

            org.bukkit.block.Chest block =
                    (org.bukkit.block.Chest) chest.getLocation().getLeftLocation().toLoc().getBlock().getState();
            if (block.getInventory().isEmpty()) return;

            // Clone before getSellValue mutates the array in-place, avoiding a second getContents() call
            ItemStack[] original = block.getInventory().getContents();
            ItemStack[] items = original.clone();
            double total = IRShopHook.getSellValue(items);

            if (total <= 0) return;

            int removed = 0;
            for (int i = 0; i < items.length; i++) {
                if (items[i] == null && original[i] != null && !original[i].getType().isAir()) {
                    removed += original[i].getAmount();
                }
            }

            double effectiveMultiplier = UpgradeManager.multiplierUpgrades ? chest.getMultiplier() : 1.0;

            if (onlinePlayer != null) {
                if (onlinePlayer.hasPermission("irshop.sell.2")) {
                    effectiveMultiplier += 1.0;
                } else if (onlinePlayer.hasPermission("irshop.sell.1.5")) {
                    effectiveMultiplier += 0.5;
                } else if (onlinePlayer.hasPermission("irshop.sell.1.25")) {
                    effectiveMultiplier += 0.25;
                }
            } else {
                // Only reach here when onlineOwner=false; create OfflinePlayer once
                OfflinePlayer offlineOwner = Bukkit.getOfflinePlayer(ownerUUID);
                effectiveMultiplier += IRShopHook.getPermissionBonus(offlineOwner) / 2.0;
            }

            total *= effectiveMultiplier;

            block.getInventory().setContents(items);

            chest.addItemsSold(removed);
            chest.addIncome(total);

            OfflinePlayer owner = onlinePlayer != null ? onlinePlayer : Bukkit.getOfflinePlayer(ownerUUID);
            IRShopHook.deposit(owner, total);

            this.handleLogs(chest, owner, total, removed);
        } catch (Exception e) {
            Logger.warn("Exception occurred while processing chest: ID: " + chest.getId()
                    + " | Location: World '" + chest.getLocation().getLeftLocation().world
                    + "', x" + chest.getLocation().getLeftLocation().x
                    + ", y" + chest.getLocation().getLeftLocation().y
                    + ", z" + chest.getLocation().getLeftLocation().z
                    + " | TotalProfit: $" + chest.getIncome(null)
                    + " | TotalItemsSold: " + chest.getItemsSold());
            if (e instanceof ClassCastException) {
                Logger.warn("The chest at this location does not longer exist, removing chest from database...");
                this.plugin.getManager().removeChest(
                        new ChestLocation(chest.getLocation().getLeftLocation().toLoc()));
            }
            if (this.plugin.debug) {
                e.printStackTrace();
            }
        }
    }

    private void handleLogs(Chest chest, OfflinePlayer owner, double profit, int items) {
        if (chest.isLogging() && this.plugin.getManager().soldItemsLoggingPlayer && owner.isOnline()) {
            Logger.sendPlayerMessage((Player) owner,
                    Lang.ITEMS_SOLD_PLAYER_LOG.get()
                            .replace("%profit%", this.plugin.formatPrices(profit, Lang.ITEMS_SOLD_PLAYER_LOG.get()))
                            .replace("%chest-name%", chest.getName())
                            .replace("%amount%", String.valueOf(items))
                            .replace("%id%", String.valueOf(chest.getId())));
        }
        if (this.intervalLogging) {
            if (this.logger == null) {
                Logger.info(Lang.ITEMS_SOLD_CONSOLE_LOG.get()
                        .replace("%profit%", this.plugin.formatPrices(profit, Lang.ITEMS_SOLD_CONSOLE_LOG.get()))
                        .replace("%chest-name%", ChatColor.stripColor(chest.getName()).replace("%player%", owner.getName()))
                        .replace("%location%", "world '" + chest.getLocation().getLeftLocation().world
                                + "', x" + chest.getLocation().getLeftLocation().x
                                + ", y" + chest.getLocation().getLeftLocation().y
                                + ", z" + chest.getLocation().getLeftLocation().z)
                        .replace("%amount%", String.valueOf(items))
                        .replace("%id%", String.valueOf(chest.getId())));
            } else {
                this.logger.addContents(items, chest.getId());
            }
        }
    }

    public void stop() {
        this.running = false;
        this.SCHEDULER_THREAD.shutdownNow();
    }

    public void reload() {
        this.running = false;
        this.queue.reload();
        this.running = true;
    }

    public void queueChest(Chest chest) {
        this.queue.addChest(chest);
    }

    public void updateChest(Chest chest, int newIntervalID) {
        this.queue.updateChestInterval(chest, newIntervalID);
    }

    public void removeFromQueue(Chest chest) {
        this.queue.removeChest(chest);
    }
}
