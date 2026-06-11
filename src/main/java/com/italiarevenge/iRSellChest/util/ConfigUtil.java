/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.configuration.ConfigurationSection
 *  org.bukkit.configuration.MemorySection
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.configuration.serialization.ConfigurationSerializable
 *  org.yaml.snakeyaml.Yaml
 */
package com.italiarevenge.iRSellChest.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.util.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.yaml.snakeyaml.Yaml;

public class ConfigUtil {
    public static void updateConfig(AutoSellChests plugin) {
        int configVer = Integer.parseInt(Config.get().getString("config-version", "1.0.0").replace(".", ""));
        if (configVer < Integer.parseInt(YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(plugin.getResource("config.yml"))).getString("config-version").replace(".", ""))) {
            Logger.info("Updating configs to newer version");
            if (configVer == 100) {
                int max = Config.get().getInt("player-max-sellchests");
                if (max != 0) {
                    Config.get().set("max-sellchests.default", (Object)max);
                }
                configVer = 101;
            }
            if (configVer == 101) {
                Config.get().set("compatibility-mode", (Object)true);
                configVer = 102;
            }
            if (configVer == 102) {
                plugin.newPriceFormat = false;
                configVer = 110;
            }
            if (configVer == 110) {
                Config.get().set("chest-holograms.display-range", (Object)10);
                File file = new File(plugin.getDataFolder(), "menus.yml");
                YamlConfiguration menuConfig = plugin.loadConfiguration(file, "menus.yml");
                if (menuConfig.getInt("settings-menu.items.logging-item.slot") == 3 && menuConfig.getInt("settings-menu.items.rename-item.slot") == 7) {
                    ConfigurationSection item = menuConfig.createSection("settings-menu.items.hologram-item");
                    item.set("material", (Object)"ARMOR_STAND");
                    item.set("name", (Object)"%translations-toggle-chest-hologram%");
                    item.set("lore", Collections.singletonList("%translations-current-value%"));
                    item.set("slot", (Object)5);
                    menuConfig.set("settings-menu.items.hologram-item", (Object)item);
                    menuConfig.set("settings-menu.items.logging-item.slot", (Object)2);
                    menuConfig.set("settings-menu.items.rename-item.slot", (Object)8);
                    ConfigUtil.save((FileConfiguration)menuConfig, file);
                }
                String[] romanNumerals = new String[]{"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
                ArrayList intervalUpgrades = new ArrayList(Config.get().getConfigurationSection("interval-upgrades").getKeys(false));
                for (int i = 0; i < intervalUpgrades.size(); ++i) {
                    if (Config.get().getString("interval-upgrades." + (String)intervalUpgrades.get(i) + ".lvl-name", null) != null) continue;
                    Config.get().set("interval-upgrades." + (String)intervalUpgrades.get(i) + ".lvl-name", (Object)romanNumerals[i]);
                }
                ArrayList multiplierUpgrades = new ArrayList(Config.get().getConfigurationSection("multiplier-upgrades").getKeys(false));
                for (int i = 0; i < multiplierUpgrades.size(); ++i) {
                    if (Config.get().getString("multiplier-upgrades." + (String)multiplierUpgrades.get(i) + ".lvl-name", null) != null) continue;
                    Config.get().set("multiplier-upgrades." + (String)multiplierUpgrades.get(i) + ".lvl-name", (Object)romanNumerals[i]);
                }
                configVer = 120;
            }
            if (configVer == 120) {
                String name = Config.get().getString("sellchest-name");
                if (name != null) {
                    Config.get().set("sell-chest-item.name", (Object)name);
                    Config.get().set("sellchest-name", null);
                }
                List lore = Config.get().getStringList("sellchest-lore");
                Config.get().set("sell-chest-item.lore", (Object)lore);
                Config.get().set("sellchest-lore", null);
                configVer = 130;
            }
            if (configVer == 130) {
                Config.get().set("chest-holograms.update-interval", (Object)"1s");
                configVer = 131;
            }
            Config.get().set("config-version", (Object)ConfigUtil.getConfigVersion(configVer));
            Config.save();
            Config.reload();
        }
    }

    private static String getConfigVersion(int version) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < String.valueOf(version).toCharArray().length; ++i) {
            if (String.valueOf(version).toCharArray().length != i + 1) {
                builder.append(String.valueOf(version).toCharArray()[i]).append(".");
                continue;
            }
            builder.append(String.valueOf(version).toCharArray()[i]);
        }
        return builder.toString();
    }

    public static void save(FileConfiguration fileConfiguration, File file) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(file), StandardCharsets.UTF_8));
            Map<String, String> comments = ConfigUtil.getComments(input.lines().collect(Collectors.toList()));
            input.close();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(file), StandardCharsets.UTF_8));
            for (String key : fileConfiguration.getKeys(true)) {
                Object obj;
                String[] keys = key.split("\\.");
                String actualKey = keys[keys.length - 1];
                String comment = comments.remove(key);
                StringBuilder prefixBuilder = new StringBuilder();
                int indents = keys.length - 1;
                ConfigUtil.appendPrefixSpaces(prefixBuilder, indents);
                String prefixSpaces = prefixBuilder.toString();
                if (comment != null) {
                    writer.write(comment);
                }
                if ((obj = fileConfiguration.get(key)) instanceof ConfigurationSerializable) {
                    writer.write(prefixSpaces + actualKey + ": " + new Yaml().dump((Object)((ConfigurationSerializable)obj).serialize()));
                    continue;
                }
                if (obj instanceof String || obj instanceof Character) {
                    if (obj instanceof String) {
                        String s = (String)obj;
                        obj = s.replace("\n", "\\n");
                    }
                    writer.write(prefixSpaces + actualKey + ": " + new Yaml().dump(obj).replace("\n ", ""));
                    continue;
                }
                if (obj instanceof List) {
                    writer.write(ConfigUtil.getListAsString((List)obj, actualKey, prefixSpaces));
                    continue;
                }
                if (obj instanceof MemorySection) {
                    writer.write(prefixSpaces + actualKey + ":\n");
                    continue;
                }
                writer.write(prefixSpaces + actualKey + ": " + new Yaml().dump(obj));
            }
            String danglingComments = comments.get(null);
            if (danglingComments != null) {
                writer.write(danglingComments);
            }
            writer.close();
        }
        catch (IOException e) {
            AutoSellChests.getInstance().getLogger().warning("Error while saving " + file.getName());
            e.printStackTrace();
        }
    }

    private static Map<String, String> getComments(List<String> lines) {
        HashMap<String, String> comments = new HashMap<String, String>();
        StringBuilder builder = new StringBuilder();
        StringBuilder keyBuilder = new StringBuilder();
        int lastLineIndentCount = 0;
        for (String line : lines) {
            if (line != null && line.trim().startsWith("-")) continue;
            if (line == null || line.trim().equals("") || line.trim().startsWith("#")) {
                builder.append(line).append("\n");
                continue;
            }
            lastLineIndentCount = ConfigUtil.setFullKey(keyBuilder, line, lastLineIndentCount);
            if (keyBuilder.length() <= 0) continue;
            comments.put(keyBuilder.toString(), builder.toString());
            builder.setLength(0);
        }
        if (builder.length() > 0) {
            comments.put(null, builder.toString());
        }
        return comments;
    }

    private static int setFullKey(StringBuilder keyBuilder, String configLine, int lastLineIndentCount) {
        int currentIndents = ConfigUtil.countIndents(configLine);
        String key = configLine.trim().split(":")[0];
        if (keyBuilder.length() == 0) {
            keyBuilder.append(key);
        } else if (currentIndents == lastLineIndentCount) {
            ConfigUtil.removeLastKey(keyBuilder);
            if (keyBuilder.length() > 0) {
                keyBuilder.append(".");
            }
            keyBuilder.append(key);
        } else if (currentIndents > lastLineIndentCount) {
            keyBuilder.append(".").append(key);
        } else {
            int difference = lastLineIndentCount - currentIndents;
            for (int i = 0; i < difference + 1; ++i) {
                ConfigUtil.removeLastKey(keyBuilder);
            }
            if (keyBuilder.length() > 0) {
                keyBuilder.append(".");
            }
            keyBuilder.append(key);
        }
        return currentIndents;
    }

    private static String getListAsString(List list, String actualKey, String prefixSpaces) {
        StringBuilder builder = new StringBuilder(prefixSpaces).append(actualKey).append(":");
        if (list.isEmpty()) {
            builder.append(" []\n");
            return builder.toString();
        }
        builder.append("\n");
        for (int i = 0; i < list.size(); ++i) {
            Object o = list.get(i);
            if (o instanceof String || o instanceof Character) {
                String s = o.toString();
                if (s.contains("\"") || s.contains("'")) {
                    s = s.replace("'", "''");
                }
                builder.append(prefixSpaces).append("- '").append(s).append("'");
            } else if (o instanceof List) {
                builder.append(prefixSpaces).append("- ").append(new Yaml().dump(o));
            } else {
                builder.append(prefixSpaces).append("- ").append(o);
            }
            if (i == list.size()) continue;
            builder.append("\n");
        }
        return builder.toString();
    }

    private static int countIndents(String s) {
        int spaces = 0;
        for (char c : s.toCharArray()) {
            if (c != ' ') break;
            ++spaces;
        }
        return spaces / 2;
    }

    private static void removeLastKey(StringBuilder keyBuilder) {
        String temp = keyBuilder.toString();
        String[] keys = temp.split("\\.");
        if (keys.length == 1) {
            keyBuilder.setLength(0);
            return;
        }
        temp = temp.substring(0, temp.length() - keys[keys.length - 1].length() - 1);
        keyBuilder.setLength(temp.length());
    }

    private static String getPrefixSpaces(int indents) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indents; ++i) {
            builder.append("  ");
        }
        return builder.toString();
    }

    private static void appendPrefixSpaces(StringBuilder builder, int indents) {
        builder.append(ConfigUtil.getPrefixSpaces(indents));
    }
}

