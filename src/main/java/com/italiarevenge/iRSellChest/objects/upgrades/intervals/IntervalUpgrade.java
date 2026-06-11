package com.italiarevenge.iRSellChest.objects.upgrades.intervals;

import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.hooks.IRShopHook;
import com.italiarevenge.iRSellChest.managers.UpgradeManager;
import com.italiarevenge.iRSellChest.objects.ChestUpgrade;
import com.italiarevenge.iRSellChest.objects.upgrades.ChestInterval;
import com.italiarevenge.iRSellChest.util.Logger;
import com.italiarevenge.iRSellChest.util.TimeUtils;
import com.italiarevenge.iRSellChest.util.exceptions.UpgradeLoadException;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IntervalUpgrade implements ChestInterval, ChestUpgrade {

    private final int level;
    private final Material item;
    private final String name;
    private final String lvlName;
    private final List<String> lore;
    private final boolean enchanted;
    private final double price;
    private final String permission;
    private final long interval;
    private final long ticks;

    public IntervalUpgrade(ConfigurationSection section, int level) throws UpgradeLoadException {
        if (section == null) throw new UpgradeLoadException("Failed to load upgrade data", null);
        this.level = level;
        this.price = loadPrice(section.getString("price"));
        this.permission = section.getString("permission");
        this.interval = this.getSellInterval(section.getString("interval"));
        this.ticks = this.interval / 1000L * 20L;
        this.name = Lang.formatColors(section.getString("name"));
        if (this.name == null) throw new UpgradeLoadException("Failed to get name of upgrade", null);
        this.lvlName = Lang.formatColors(section.getString("lvl-name", String.valueOf(level)));
        this.lore = section.getStringList("lore").stream().map(Lang::formatColors).collect(Collectors.toList());
        this.enchanted = section.getBoolean("enchanted");
        try {
            this.item = Material.valueOf(section.getString("item"));
        } catch (IllegalArgumentException e) {
            throw new UpgradeLoadException("Failed to get item material of upgrade for '" + section.getString("item") + "'", null);
        }
    }

    @Override
    public String getName() { return this.name; }

    @Override
    public String getLevelName() { return this.lvlName; }

    @Override
    public ItemStack getUpgradeItem(boolean doubleChest) {
        ItemStack item = new ItemStack(this.item);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(this.name);
        meta.setLore(this.getLore(doubleChest));
        if (this.enchanted) {
            if (AutoSellChests.getInstance().version >= 121) {
                meta.setEnchantmentGlintOverride(Boolean.valueOf(true));
            } else {
                meta.addItemFlags(ItemFlag.values());
                meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    private List<String> getLore(boolean doubleChest) {
        ChestUpgrade nextUpgrade = UpgradeManager.getIntervalUpgrade(this.level + 1);
        if (nextUpgrade != null) {
            ArrayList<String> lore = new ArrayList<>(this.lore);
            lore.replaceAll(s -> s.replace("%next-upgrade-cost%", nextUpgrade.getPrice(doubleChest)));
            return lore;
        }
        return this.lore;
    }

    @Override
    public boolean buy(Player p, boolean doubleChest) {
        double finalPrice = doubleChest ? this.price * 2.0 : this.price;
        if (IRShopHook.getBalance((OfflinePlayer) p) < finalPrice) {
            Logger.sendPlayerMessage(p, AutoSellChests.getInstance()
                    .replaceColoredPlaceholder(Lang.INSUFFICIENT_FUNDS_UPGRADE.get(), "%ecoType%", "Money"));
            return false;
        }
        if (this.permission != null && !this.permission.isEmpty() && !p.hasPermission(this.permission)) {
            Logger.sendPlayerMessage(p, Lang.NO_UPGRADE_PERMISSIONS.get());
            return false;
        }
        IRShopHook.withdraw((OfflinePlayer) p, finalPrice);
        return true;
    }

    @Override
    public String getPrice(boolean doubleChest) {
        return AutoSellChests.getInstance().formatPrice(doubleChest ? this.price * 2.0 : this.price);
    }

    @Override
    public long getInterval() { return this.interval; }

    @Override
    public long getTicks() { return this.ticks; }

    private double loadPrice(String price) throws UpgradeLoadException {
        try {
            if (price.contains("::")) {
                return Double.parseDouble(price.split("::")[1]);
            }
            return Double.parseDouble(price);
        } catch (NullPointerException | NumberFormatException e) {
            throw new UpgradeLoadException("Failed to load upgrade price, amount for '" + price + "' is invalid", null);
        }
    }

    private long getSellInterval(String time) throws UpgradeLoadException {
        try {
            return TimeUtils.getTime(time);
        } catch (NullPointerException | ParseException e) {
            throw new UpgradeLoadException("Invalid sell interval for " + time, e);
        }
    }
}
