/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package com.italiarevenge.iRSellChest.managers;

import java.io.File;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.util.ConfigUtil;
import com.italiarevenge.iRSellChest.util.Logger;
import com.italiarevenge.iRSellChest.util.SimpleInventoryBuilder;
import com.italiarevenge.iRSellChest.util.exceptions.InventoryLoadException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class InventoryManager {
    private YamlConfiguration menusConfig;
    private SimpleInventoryBuilder upgradeInv;
    private SimpleInventoryBuilder settingsInv;
    private SimpleInventoryBuilder mainInv;

    public InventoryManager() {
        this.init();
    }

    public SimpleInventoryBuilder getMainInv() {
        return this.mainInv;
    }

    public SimpleInventoryBuilder getSettingsInv() {
        return this.settingsInv;
    }

    public SimpleInventoryBuilder getUpgradeInv() {
        return this.upgradeInv;
    }

    public void reload() {
        this.init();
    }

    private void init() {
        this.menusConfig = this.loadConfig(AutoSellChests.getInstance());
        this.mainInv = this.loadInventory("sell-chest-menu");
        this.settingsInv = this.loadInventory("settings-menu");
        this.upgradeInv = this.loadInventory("upgrade-menu");
        Logger.info("Completed loading custom inventory's from menus.yml");
    }

    private SimpleInventoryBuilder loadInventory(String path) {
        try {
            return new SimpleInventoryBuilder(this.menusConfig.getConfigurationSection(path));
        }
        catch (InventoryLoadException e) {
            Logger.warn(e.getMessage());
        }
        catch (Exception e) {
            Logger.warn("Failed to load inventory at " + path + " from menus.yml with error:");
            e.printStackTrace();
        }
        return null;
    }

    private YamlConfiguration loadConfig(AutoSellChests plugin) {
        File file = new File(plugin.getDataFolder(), "menus.yml");
        if (file.exists()) {
            ConfigUtil.save((FileConfiguration)plugin.loadConfiguration(file, "menus.yml"), file);
        } else {
            AutoSellChests.getInstance().saveResource("menus.yml", false);
        }
        return plugin.loadConfiguration(file, "menus.yml");
    }
}

