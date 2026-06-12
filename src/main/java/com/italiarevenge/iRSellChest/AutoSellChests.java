package com.italiarevenge.iRSellChest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.italiarevenge.iRSellChest.commands.SellChestCommand;
import com.italiarevenge.iRSellChest.database.SQLite;
import com.italiarevenge.iRSellChest.events.BlockListener;
import com.italiarevenge.iRSellChest.events.ChunkListener;
import com.italiarevenge.iRSellChest.events.PlayerListener;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.hooks.IRShopHook;
import com.italiarevenge.iRSellChest.managers.AFKDetection.AFKDetectionCMI;
import com.italiarevenge.iRSellChest.managers.AFKDetection.AFKDetectionEssentials;
import com.italiarevenge.iRSellChest.managers.AFKManager;
import com.italiarevenge.iRSellChest.managers.ChestManager;
import com.italiarevenge.iRSellChest.managers.HologramManager;
import com.italiarevenge.iRSellChest.managers.InventoryManager;
import com.italiarevenge.iRSellChest.managers.UpgradeManager;
import com.italiarevenge.iRSellChest.metrics.Metrics;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.util.ConfigUtil;
import com.italiarevenge.iRSellChest.util.Logger;
import com.italiarevenge.iRSellChest.util.TimeUtils;
import com.italiarevenge.iRSellChest.util.Version;
import com.italiarevenge.iRSellChest.util.scheduler.ServerScheduler;
import com.italiarevenge.iRSellChest.util.scheduler.Task;
import com.italiarevenge.iRSellChest.util.scheduler.schedulers.BukkitScheduler;
import com.italiarevenge.iRSellChest.util.scheduler.schedulers.FoliaScheduler;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class AutoSellChests extends JavaPlugin {

    public final int version = this.getVersion();
    private static AutoSellChests instance;
    private final SQLite database = new SQLite(this);
    private final TimeUtils timeUtils = new TimeUtils();
    private final NamespacedKey key = new NamespacedKey((Plugin) this, "autosell");
    private final ServerScheduler scheduler = this.getScheduler();
    private ChestManager manager = new ChestManager(this);
    private InventoryManager inventoryManager;
    private HologramManager hologramManager;
    private UpgradeManager upgradeManager;
    private AFKManager afkManager;
    public boolean newPriceFormat = true;
    public boolean debug;

    public static AutoSellChests getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (Bukkit.getServer().getPluginManager().getPlugin("IR-Shop") == null) {
            this.getLogger().warning("Could not find IR-Shop, disabling...");
            this.getServer().getPluginManager().disablePlugin((Plugin) this);
            return;
        }
        this.getLogger().info("Found IR-Shop, enabling...");

        if (!this.isSpigotServer()) {
            this.getLogger().warning("It seems like you are using a server type which does not support spigot, please install a supported server type like Paper which can be downloaded from here: https://papermc.io/");
            this.getServer().getPluginManager().disablePlugin((Plugin) this);
            return;
        }

        if (!IRShopHook.setupEconomy(this)) {
            this.getLogger().warning("Vault not found or no economy plugin loaded, disabling...");
            this.getServer().getPluginManager().disablePlugin((Plugin) this);
            return;
        }
        IRShopHook.setupPermissions(this);

        Metrics metrics = new Metrics(this, 15605);
        metrics.addCustomChart(new Metrics.SimplePie("esgui_ver", () -> "IR-Shop"));

        Config.setup();
        Lang.reload();
        new Logger(this);
        ConfigUtil.updateConfig(this);

        if (!this.database.connect()) {
            this.getServer().getPluginManager().disablePlugin((Plugin) this);
            return;
        }

        this.registerCommands();
        this.getServer().getPluginManager().registerEvents((Listener) new PlayerListener(this), (Plugin) this);
        this.getServer().getPluginManager().registerEvents((Listener) new BlockListener(this), (Plugin) this);
        this.getServer().getPluginManager().registerEvents((Listener) new ChunkListener(this), (Plugin) this);

        this.debug = Config.get().getBoolean("debug");

        this.runTaskLater(() -> {
            if (Config.get().getBoolean("afk-prevention", false)) {
                this.afkManager = this.getAfkManager();
            }
            this.inventoryManager = new InventoryManager();
            this.hologramManager = new HologramManager(this);
            this.upgradeManager = new UpgradeManager(this);
            this.manager.load();
        }, 5L);
    }

    @Override
    public void onDisable() {
        try {
            if (this.manager != null) {
                this.manager.disable();
            }
        } finally {
            if (this.database != null) {
                this.database.closeConnection();
            }
        }
    }

    private void registerCommands() {
        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register("autosellchests", (Command) new SellChestCommand(this));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Logger.warn("Exception occurred registering commands.");
            e.printStackTrace();
        }
    }

    public TimeUtils getTimeUtils() {
        return this.timeUtils;
    }

    public SQLite getDatabase() {
        return this.database;
    }

    public NamespacedKey getKey() {
        return this.key;
    }

    public ChestManager getManager() {
        return this.manager;
    }

    public AFKManager getAFKManager() {
        return this.afkManager;
    }

    public InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public HologramManager getHologramManager() {
        return this.hologramManager;
    }

    public void reloadManager() {
        this.manager.disable();
        IRShopHook.invalidateCache();
        if (Config.get().getBoolean("afk-prevention", false)) {
            this.afkManager = this.getAfkManager();
        }
        this.upgradeManager.reload();
        this.inventoryManager.reload();
        this.manager.load();
    }

    public Task runTaskTimer(Runnable runnable, long delay, long period) {
        return this.scheduler.runTaskTimer(this, runnable, delay, period);
    }

    public void runTaskLater(Runnable runnable, long delay) {
        this.scheduler.runTaskLater(this, runnable, delay);
    }

    public void runTaskLater(Runnable runnable, Location loc, long delay) {
        this.scheduler.runTaskLater(this, loc, runnable, delay);
    }

    public void runTask(Runnable runnable) {
        this.scheduler.runTask(this, runnable);
    }

    public void runTask(Chest chest, Runnable runnable) {
        this.scheduler.runTask(this, chest, runnable);
    }

    public void runTaskAsync(Runnable runnable) {
        this.scheduler.runTaskAsync(this, runnable);
    }

    public Task runTaskAsyncTimer(Runnable runnable, long delay, long period) {
        return this.scheduler.runTaskAsyncTimer(this, runnable, delay, period);
    }

    private Integer getVersion() {
        String version = Bukkit.getBukkitVersion().split("-")[0].split("\\.build")[0];
        if (version.chars().filter(c -> c == '.').count() == 2) {
            return Integer.valueOf(version.substring(0, version.lastIndexOf(".")).replace(".", ""));
        }
        return Integer.valueOf(version.replace(".", ""));
    }

    public YamlConfiguration loadConfiguration(File file, String fileName) {
        if (file == null) throw new IllegalArgumentException("Cannot load " + fileName + " config file.");
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load((Reader) new InputStreamReader((InputStream) new FileInputStream(file), StandardCharsets.UTF_8));
            return config;
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            this.getLogger().warning("Cannot read " + fileName + " config because it is mis-configured.");
            e.printStackTrace();
        }
        return null;
    }

    public String formatPrice(double price) {
        return IRShopHook.formatPrice(price);
    }

    public String formatPrices(double price, String message) {
        String formatted = IRShopHook.formatPrice(price);
        String color = message != null && message.contains("%profit%")
                ? ChatColor.getLastColors(message.split("%profit%")[0]) : null;
        return formatted + "§r" + (color == null ? "" : color);
    }

    public String replaceColoredPlaceholder(String message, String placeholder, String replacement) {
        String c = ChatColor.getLastColors(message.split(placeholder)[0]);
        return message.replace(placeholder, replacement + "§r" + c);
    }

    public static List<String> splitLongString(String string) {
        ArrayList<String> list = new ArrayList<>();
        String[] words = string.split(" ");
        int e = 0;
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if ((e += ChatColor.stripColor(word).length() + 1) >= 42) {
                list.add(line.toString());
                e = word.length();
                line = new StringBuilder();
                line.append(ChatColor.getLastColors(list.get(list.size() - 1)));
                line.append(word);
                continue;
            }
            if (line.length() != 0) {
                line.append(" ");
            }
            line.append(word);
        }
        list.addAll(Arrays.asList(line.toString().split("\n")));
        return list;
    }

    private AFKManager getAfkManager() {
        PluginManager pm = this.getServer().getPluginManager();
        if (pm.getPlugin("Essentials") != null) {
            return new AFKDetectionEssentials(this);
        }
        if (pm.getPlugin("CMI") != null) {
            return new AFKDetectionCMI(this);
        }
        return null;
    }

    private boolean isSpigotServer() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private ServerScheduler getScheduler() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return new FoliaScheduler(this);
        } catch (ClassNotFoundException e) {
            return new BukkitScheduler(this);
        }
    }
}
