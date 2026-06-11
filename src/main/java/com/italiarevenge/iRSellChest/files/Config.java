/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.Configuration
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package com.italiarevenge.iRSellChest.files;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.util.ConfigUtil;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
    private static File file;
    private static FileConfiguration config;
    private static final String fileName = "config.yml";

    public static boolean setup() {
        file = new File(AutoSellChests.getInstance().getDataFolder(), fileName);
        Config.reload();
        return config != null;
    }

    public static FileConfiguration get() {
        return config;
    }

    public static void save() {
        ConfigUtil.save(config, file);
    }

    public static void reload() {
        if (file.exists()) {
            YamlConfiguration c = AutoSellChests.getInstance().loadConfiguration(file, fileName);
            if (c == null) {
                return;
            }
            AutoSellChests.getInstance().saveResource(fileName, true);
            YamlConfiguration conf = AutoSellChests.getInstance().loadConfiguration(file, fileName);
            for (String str : c.getKeys(false)) {
                conf.set(str, c.get(str));
            }
            ConfigUtil.save((FileConfiguration)conf, file);
            config = conf;
        } else {
            AutoSellChests.getInstance().saveResource(fileName, false);
            config = AutoSellChests.getInstance().loadConfiguration(file, fileName);
        }
        config.setDefaults((Configuration)YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(AutoSellChests.getInstance().getResource(fileName))));
    }
}

