/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatColor
 *  org.bukkit.ChatColor
 *  org.bukkit.configuration.Configuration
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 */
package com.italiarevenge.iRSellChest.files;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.util.ConfigUtil;
import com.italiarevenge.iRSellChest.util.Gradient;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Lang {
    PLUGIN_PREFIX("#FF7070&lSell#CDCDCD&lChests &8>>"),
    SELLCHEST_PLACED("&aYou have successfully placed a %chest-name%, left click it for more info."),
    SELLCHEST_BROKEN("&cYou have picked-up an %chest-name%"),
    MAX_SELLCHESTS_REACHED("&cYou already have &a%maxSellChests% &cplaced and cannot place anymore"),
    ITEMS_SOLD_PLAYER_LOG("&a&lYour %chest-name% &a&lhas sold &6%amount% &a&litem(s) for &6%profit%"),
    ITEMS_SOLD_CONSOLE_INTERVAL("Completed %count% sell interval(s) for %chests% loaded sell chest(s) and sold %items% item(s) in the last %interval%."),
    ITEMS_SOLD_CONSOLE_LOG("Sell chest with id '%id%' from player %player% on location %location% has sold %amount% item(s) for %profit%"),
    NO_PERMISSIONS("&cYou do not have permissions for that"),
    NO_PERMISSIONS_FOR_CHEST("&cYou do not have permissions to access this #FF7070Sell#CDCDCDChest"),
    SELL_CHEST_GIVEN_LOG("%player_name% was given %amount% SellChests"),
    SELL_CHEST_MULTIPLIER_GIVEN_LOG("%player_name% was given %amount% SellChests with a %multiplier%% sell multiplier"),
    PLAYER_SELL_CHEST_GIVEN("&cYou were given &7%amount% #FF7070Sell#CDCDCDChests"),
    NOT_ENOUGH_INVENTORY_SPACE("&cYou do not have enough space in your inventory"),
    PLACED_SELL_CHESTS_ACTION_BAR("&6&lPlaced sell chests: &c%amount%&c&4/&c%limit%"),
    PLACED_SELL_CHESTS_ACTION_BAR_MAX("\u221e"),
    PLACED_SELL_CHESTS_BOSS_BAR("&a&lPlaced sell chests:"),
    CANNOT_FORM_DOUBLE_CHEST("&cThe sell chest cannot be a placed against a non sell chest"),
    CANNOT_FORM_DOUBLE_CHEST_MISMATCH("&cCannot form double sell chest with different upgrades"),
    CANNOT_PLACE_SELL_CHEST_HERE("&cYou may not place a #FF7070Sell#CDCDCDChest &chere"),
    CANNOT_PLACE_CHEST_AGAINST_SELL_CHEST("&cThis chest cannot be placed against a sell chest"),
    CANNOT_REMOVE_SELL_CHEST("&cYou do not have the required permissions to remove this sell chest"),
    LOCATION_FORMAT("World '%world%', x%pos_x%, y%pos_y%, z%pos_z%"),
    DESTROY_CHEST("&c&lPick up this #CDCDCD&lAuto#FF7070&lSell#CDCDCD&lChest"),
    SELL_CHEST_BLOCK_INFO("#FF7070&lChest info:"),
    SELL_CHEST_OWNER("&eOwner: #FF7070%player_name%"),
    SELL_CHEST_LOCATION("&eLocation: #FF7070%loc%"),
    SELL_CHEST_ID("&eChest ID: #FF7070%id%"),
    SELL_CHEST_INTERVAL("&eCurrent interval: #FF7070%interval-name%&f(%interval%)"),
    SELL_CHEST_MULTIPLIER("&eCurrent multiplier: #FF7070%multiplier-name%&f(x%multiplier%)"),
    SELL_CHEST_NEXT_SELL("&eNext sell interval: #FF7070%time%"),
    INFO_SCREEN_TITLE("&8&lChest information"),
    SOLD_ITEMS_INFO("&c&lTotal items sold: &a%amount%"),
    INCOME_INFO("&c&lTotal profit made: &a%profit%"),
    CLAIM_ABLE_INFO("&a&lClick to view unclaimed profits."),
    SELL_CHEST_SETTINGS("<gradient:#FF4A4A>Chest settings</gradient:#FFDBDB>"),
    SELL_CHEST_UPGRADES("<gradient:#FF4A4A>Chest upgrades</gradient:#FFDBDB>"),
    AVAILABLE_PROFIT_MENU_TITLE("&8&lAvailable profits"),
    AVAILABLE_PROFIT("&6&lClick to claim &a%profit%&6&l."),
    CANNOT_CLAIM_PROFIT("&cUnable to claim this right now."),
    CHEST_SETTINGS_TITLE("&8&lChest settings"),
    TOGGLE_SOLD_ITEMS_LOGGING("&c&lToggle sold item logging"),
    CURRENT_VALUE("&eCurrent value: &a%value%"),
    CHANGE_CHEST_NAME("&c&lClick to update the name"),
    CURRENT_DISPLAYNAME("&eCurrent: &a%chest-name%"),
    TOGGLE_CHEST_HOLOGRAM("&c&lClick to toggle the chest hologram"),
    ENTER_NAME_MENU_TITLE("&8&lEnter new name"),
    CHEST_UPGRADE_TITLE("&8&lUpgrades"),
    INSUFFICIENT_FUNDS_UPGRADE("&cYou do not have enough %ecoType% to purchase this upgrade."),
    CHEST_INTERVAL_UPGRADED("&aYou bought %upgrade-name% &afor &f%upgrade-cost%&a, this chest will now sell its contents every &e%interval%"),
    CHEST_MULTIPLIER_UPGRADED("&aYou bought %upgrade-name% &afor &f%upgrade-cost%&a, this chest will now sell items at a increased price of &ex%multiplier%"),
    NO_UPGRADE_PERMISSIONS("&cYou do not have the required permission to buy this upgrade");

    private String def;
    private static File configFile;
    private static FileConfiguration config;
    private static AutoSellChests plugin;
    private static Map<String, String> messages;
    private static final Pattern rgbPattern;
    private static final Pattern startGradientPattern;
    private static final Pattern endGradientPattern;

    private Lang(String def) {
        this.def = def;
    }

    public static void reload() {
        if (!messages.isEmpty()) {
            messages.clear();
        }
        plugin = AutoSellChests.getInstance();
        configFile = new File(plugin.getDataFolder(), "lang.yml");
        Lang.loadLanguageFile();
    }

    public String getKey() {
        return this.name().toLowerCase(Locale.ENGLISH).replace("_", "-").replace("0", ",");
    }

    public void clearMessages() {
        messages.clear();
    }

    public String get() {
        String value;
        String key = this.getKey();
        if (messages.containsKey(key)) {
            return messages.get(key);
        }
        try {
            value = config.getString(key, this.def);
        }
        catch (NullPointerException npe) {
            value = this.def;
        }
        value = Lang.formatColors(value, Lang.findGradient(value));
        messages.put(key, value);
        return value;
    }

    public String raw() {
        try {
            return config.getString(this.getKey(), this.def);
        }
        catch (NullPointerException npe) {
            return this.def;
        }
    }

    private static Gradient findGradient(String value) {
        Matcher matcher = startGradientPattern.matcher(value);
        if (matcher.find()) {
            String g1 = value.substring(matcher.start(), matcher.end());
            matcher = endGradientPattern.matcher(value);
            if (matcher.find()) {
                String g2 = value.substring(matcher.start(), matcher.end());
                return new Gradient(g1, g2, value);
            }
            return null;
        }
        return null;
    }

    public static String formatColors(String value) {
        return Lang.formatColors(value, Lang.findGradient(value));
    }

    public static String formatColors(String value, Gradient gradient) {
        if (Lang.plugin.version >= 116) {
            if (gradient != null) {
                value = gradient.get();
            }
            Matcher matcher = rgbPattern.matcher(value);
            while (matcher.find()) {
                String hex = value.substring(matcher.start(), matcher.end());
                value = value.replace(hex, String.valueOf(net.md_5.bungee.api.ChatColor.of((String)hex)));
                matcher = rgbPattern.matcher(value);
            }
        }
        return ChatColor.translateAlternateColorCodes((char)'&', (String)value).replace("\\n", "\n");
    }

    private static void loadLanguageFile() {
        if (configFile.exists()) {
            YamlConfiguration c = AutoSellChests.getInstance().loadConfiguration(configFile, "lang.yml");
            if (c == null) {
                return;
            }
            AutoSellChests.getInstance().saveResource("lang.yml", true);
            YamlConfiguration conf = AutoSellChests.getInstance().loadConfiguration(configFile, "lang.yml");
            for (String str : c.getKeys(false)) {
                conf.set(str, c.get(str));
            }
            ConfigUtil.save((FileConfiguration)conf, configFile);
            config = conf;
        } else {
            AutoSellChests.getInstance().saveResource("lang.yml", false);
            config = AutoSellChests.getInstance().loadConfiguration(configFile, "lang.yml");
        }
        config.setDefaults((Configuration)YamlConfiguration.loadConfiguration((Reader)new InputStreamReader(AutoSellChests.getInstance().getResource("lang.yml"))));
    }

    static {
        messages = new HashMap<String, String>();
        rgbPattern = Pattern.compile("#[a-fA-F0-9]{6}");
        startGradientPattern = Pattern.compile("<gradient:#[a-fA-F0-9]{6}>");
        endGradientPattern = Pattern.compile("</gradient:#[a-fA-F0-9]{6}>");
    }
}

