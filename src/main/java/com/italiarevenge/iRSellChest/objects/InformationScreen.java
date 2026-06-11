package com.italiarevenge.iRSellChest.objects;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.managers.UpgradeManager;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.util.SimpleInventoryBuilder;
import com.italiarevenge.iRSellChest.util.SimplePair;
import com.italiarevenge.iRSellChest.util.TimeUtils;
import com.italiarevenge.iRSellChest.util.scheduler.Task;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Map;

public class InformationScreen implements InventoryHolder {
    private Inventory inv;
    private final Chest chest;
    private final Location selectedChest;
    private SimplePair<Integer, String> updatedString;
    private int infoItemSlot;
    private Task dynamicLore;

    public InformationScreen(Chest chest, Location location) {
        this.selectedChest = location;
        this.chest = chest;
        this.init();
    }

    private void init() {
        SimpleInventoryBuilder builder = AutoSellChests.getInstance().getInventoryManager().getMainInv();
        builder.init((InventoryHolder) this);
        builder.replace("sold-item", Collections.singletonMap("%amount%", String.valueOf(this.chest.getItemsSold())));
        builder.replace("info-item", Map.of(
                "%player_name%", Bukkit.getOfflinePlayer((UUID) this.chest.getOwner()).getName(),
                "%loc%", this.getLocation(this.selectedChest),
                "%id%", String.valueOf(this.chest.getId()),
                "%interval-name%", UpgradeManager.getIntervalUpgrade((int) this.chest.getIntervalUpgrade()).getName(),
                "%interval%", TimeUtils.getReadableTime((long) this.chest.getInterval()),
                "%multiplier-name%", UpgradeManager.getMultiplierUpgrade((int) this.chest.getMultiplierUpgrade()).getName(),
                "%multiplier%", String.valueOf(this.chest.getMultiplier())));
        this.updatedString = this.getIndex(builder.getItem("info-item"), "%time%");
        this.infoItemSlot = builder.getSlot("info-item");
        builder.replace("income-item", Collections.singletonMap("%profit%", this.chest.getIncome(Lang.INCOME_INFO.get())));
        if (this.chest.getClaimAbleValue() > 0) {
            builder.enableItem("claimable-item");
        }
        this.inv = builder.build();
        if (this.updatedString != null) {
            this.dynamicLore = this.updateTime(builder.getItem("info-item"));
        }
    }

    private String getLocation(Location loc) {
        return Lang.LOCATION_FORMAT.get()
                .replace("%world%", loc.getWorld().getName())
                .replace("%pos_x%", String.valueOf(loc.getBlockX()))
                .replace("%pos_y%", String.valueOf(loc.getBlockY()))
                .replace("%pos_z%", String.valueOf(loc.getBlockZ()));
    }

    private String getNextInterval() {
        return TimeUtils.getReadableTime((long) (this.chest.getNextInterval() - (System.currentTimeMillis() - 1000L)));
    }

    private SimplePair<Integer, String> getIndex(ItemStack item, String toFind) {
        if (!item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName() && meta.getDisplayName().contains(toFind)) {
            return new SimplePair((Object) (-1), (Object) meta.getDisplayName());
        }
        if (meta.hasLore()) {
            List lore = meta.getLore();
            for (int i = 0; i < lore.size(); ++i) {
                if (!((String) lore.get(i)).contains(toFind)) continue;
                return new SimplePair((Object) i, (Object) ((String) lore.get(i)));
            }
        }
        return null;
    }

    private Task updateTime(ItemStack item) {
        return AutoSellChests.getInstance().runTaskTimer(() -> {
            if (this.inv.getViewers().isEmpty()) {
                this.stopTask();
            }
            ItemMeta meta = item.getItemMeta();
            if ((Integer) this.updatedString.key == -1) {
                meta.setDisplayName(((String) this.updatedString.value).replace("%time%", this.getNextInterval()));
            } else {
                List lore = meta.getLore();
                lore.set((Integer) this.updatedString.key, ((String) this.updatedString.value).replace("%time%", this.getNextInterval()));
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
            this.inv.setItem(this.infoItemSlot, item);
        }, 0L, 20L);
    }

    private void stopTask() {
        this.dynamicLore.cancel();
    }

    public Chest getChest() {
        return this.chest;
    }

    public Location getSelectedChest() {
        return this.selectedChest;
    }

    public void open(Player p) {
        p.openInventory(this.inv);
    }

    public Inventory getInventory() {
        return this.inv;
    }
}
