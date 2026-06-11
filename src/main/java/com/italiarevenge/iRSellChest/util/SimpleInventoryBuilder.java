/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Material
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.InventoryHolder
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.jetbrains.annotations.Nullable
 */
package com.italiarevenge.iRSellChest.util;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.util.SimplePair;
import com.italiarevenge.iRSellChest.util.exceptions.InventoryLoadException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class SimpleInventoryBuilder {
    private final int size;
    private final String title;
    private ItemStack fillItem;
    private final Map<String, SimplePair<Integer, ItemStack>> items = new HashMap<String, SimplePair<Integer, ItemStack>>();
    private Inventory inv;

    public SimpleInventoryBuilder(ConfigurationSection config) throws InventoryLoadException {
        if (config == null) {
            throw new NullPointerException("Cannot find inventory config in menus.yml");
        }
        int size = config.getInt("gui-rows", -1);
        if (size > 6 || size < 1) {
            throw new InventoryLoadException("Invalid inventory size at '" + config.getCurrentPath() + ".gui-rows', needs to be a number from 1-6");
        }
        this.size = size * 9;
        this.loadItems(config);
        this.title = this.translate(config.getString("title"));
        if (config.contains("fill-item") && !config.getString("fill-item.material", "AIR").equalsIgnoreCase("AIR")) {
            this.fillItem = this.loadItem(config.getConfigurationSection("fill-item"));
        }
    }

    public void init(@Nullable InventoryHolder holder) {
        this.inv = Bukkit.createInventory((InventoryHolder)holder, (int)this.size, (String)this.title);
        for (String i : this.items.keySet()) {
            if (i.equals("claimable-item") || i.equals("hologram-item")) continue;
            SimplePair<Integer, ItemStack> item = this.items.get(i);
            if (item.value == null) continue;
            this.inv.setItem(((Integer)item.key).intValue(), (ItemStack)item.value);
        }
    }

    public Inventory build() {
        for (int i = 0; i < this.inv.getSize(); ++i) {
            if (this.inv.getItem(i) != null) continue;
            this.inv.setItem(i, this.fillItem);
        }
        return this.inv;
    }

    private void loadItems(ConfigurationSection config) throws InventoryLoadException {
        for (String i : config.getConfigurationSection("items").getKeys(false)) {
            if (i.contains("-") && i.split("-", 2)[1].equalsIgnoreCase("upgrade-item")) {
                this.items.put(i, new SimplePair<Integer, ItemStack>(config.getInt("items." + i + ".slot") - 1, null));
                continue;
            }
            try {
                ConfigurationSection section = config.getConfigurationSection("items." + i);
                ItemStack item = this.loadItem(section);
                if (section.contains("slot")) {
                    int slot = section.getInt("slot");
                    if (slot > this.size || slot < 1) {
                        throw new InventoryLoadException("Item slots need to be a number from 1-" + this.size);
                    }
                    this.items.put(i, new SimplePair<Integer, ItemStack>(slot - 1, item));
                    continue;
                }
                this.inv.addItem(new ItemStack[]{item});
            }
            catch (Exception e) {
                throw new InventoryLoadException("Failed to load item from menus.yml at " + config.getCurrentPath() + ".items: " + e.getMessage());
            }
        }
    }

    private ItemStack loadItem(ConfigurationSection config) throws InventoryLoadException {
        ItemStack item;
        try {
            item = new ItemStack(Material.valueOf((String)config.getString("material").toUpperCase(Locale.ROOT)), config.getInt("amount", 1));
        }
        catch (IllegalArgumentException e) {
            throw new InventoryLoadException("Invalid item material for " + config.getString("material"));
        }
        ItemMeta meta = item.getItemMeta();
        if (config.contains("name")) {
            meta.setDisplayName(this.translate(config.getString("name")));
        }
        if (config.contains("lore")) {
            meta.setLore(config.getStringList("lore").stream().map(this::translate).collect(Collectors.toList()));
        }
        if (config.getBoolean("enchantment-glint")) {
            if (AutoSellChests.getInstance().version <= 121) {
                meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
                meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            } else {
                meta.setEnchantmentGlintOverride(Boolean.valueOf(true));
            }
        }
        if (config.contains("CustomModelData")) {
            meta.setCustomModelData(Integer.valueOf(config.getInt("CustomModelData")));
        }
        item.setItemMeta(meta);
        return item;
    }

    public int getSlot(String item) {
        if (!this.items.containsKey(item)) {
            return -1;
        }
        return (Integer)this.items.get((Object)item).key;
    }

    public ItemStack getItem(String item) {
        return this.inv.getItem(((Integer)this.items.get((Object)item).key).intValue());
    }

    public void enableItem(String i) {
        SimplePair<Integer, ItemStack> item = this.items.get(i);
        this.inv.setItem(((Integer)item.key).intValue(), (ItemStack)item.value);
    }

    public void insertItem(String i, ItemStack item) {
        this.inv.setItem(((Integer)this.items.get((Object)i).key).intValue(), item);
    }

    public void replace(String i, Map<String, String> replacements) {
        boolean updated;
        if (!this.items.containsKey(i)) {
            return;
        }
        int slot = (Integer)this.items.get((Object)i).key;
        ItemStack item = this.inv.getItem(slot).clone();
        if (!item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
            String original = meta.getDisplayName();
            updated = false;
            for (Map.Entry<String, String> replacement : replacements.entrySet()) {
                if (!original.contains(replacement.getKey())) continue;
                original = original.replace(replacement.getKey(), replacement.getValue());
                updated = true;
            }
            if (updated) {
                meta.setDisplayName(original);
            }
        }
        if (meta.hasLore()) {
            List lore = meta.getLore();
            updated = false;
            for (int e = 0; e < lore.size(); ++e) {
                String original = (String)lore.get(e);
                for (Map.Entry<String, String> replacement : replacements.entrySet()) {
                    original = original.replace(replacement.getKey(), replacement.getValue());
                    updated = true;
                }
                lore.set(e, original);
            }
            if (updated) {
                meta.setLore(lore);
            }
        }
        item.setItemMeta(meta);
        this.inv.setItem(slot, item);
    }

    private String translate(String s) {
        if (s == null) {
            return "";
        }
        if (s.contains("%translations-")) {
            try {
                Lang placeholder = Lang.valueOf(s.split("%translations-")[1].split("%")[0].toUpperCase(Locale.ENGLISH).replace("-", "_"));
                s = s.replace("%translations-" + placeholder.getKey() + "%", placeholder.get());
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        return Lang.formatColors(s, null);
    }
}

