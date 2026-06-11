package com.italiarevenge.iRSellChest.objects;

import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.managers.ChestManager;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.objects.ChestInventory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ClaimProfitsScreen extends ChestInventory {

    private final Inventory inv;
    private final Chest chest;
    private final Location selectedChest;
    private boolean update;

    public ClaimProfitsScreen(Chest chest, Location selectedChest) {
        this.inv = Bukkit.createInventory((InventoryHolder) this, 9, Lang.AVAILABLE_PROFIT_MENU_TITLE.get());
        this.selectedChest = selectedChest;
        this.chest = chest;
        this.init();
    }

    private void init() {
        double claimable = this.chest.getClaimAbleValue();
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Lang.AVAILABLE_PROFIT.get()
                .replace("%profit%", AutoSellChests.getInstance().formatPrice(claimable)));
        item.setItemMeta(meta);
        this.inv.setItem(0, item);
        for (int i = 1; i < this.inv.getSize(); i++) {
            this.inv.setItem(i, ChestManager.getFillItem());
        }
    }

    public boolean hasClaimable() {
        return this.chest.getClaimAbleValue() > 0;
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
        AutoSellChests.getInstance().runTaskLater(() -> this.update = false, 1L);
    }

    public void updateInventory(Player p) {
        this.update();
        this.inv.clear();
        this.init();
        this.open(p);
    }
}
