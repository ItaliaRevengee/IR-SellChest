/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Chunk
 *  org.bukkit.Location
 *  org.bukkit.Material
 *  org.bukkit.NamespacedKey
 *  org.bukkit.World
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.Recipe
 *  org.bukkit.inventory.ShapedRecipe
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.persistence.PersistentDataType
 *  org.bukkit.plugin.Plugin
 *  org.jetbrains.annotations.Nullable
 */
package com.italiarevenge.iRSellChest.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.managers.UpgradeManager;
import com.italiarevenge.iRSellChest.objects.Chest;
import com.italiarevenge.iRSellChest.objects.ChestLocation;
import com.italiarevenge.iRSellChest.objects.ChestSettings;
import com.italiarevenge.iRSellChest.objects.ChunkLoc;
import com.italiarevenge.iRSellChest.scheduler.MainScheduler;
import com.italiarevenge.iRSellChest.util.Logger;
import com.italiarevenge.iRSellChest.util.TimeUtils;
import com.italiarevenge.iRSellChest.util.exceptions.InvalidIngredientException;
import com.italiarevenge.iRSellChest.util.exceptions.InvalidPatternException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class ChestManager {
    public static String chestName;
    private AutoSellChests plugin;
    private MainScheduler scheduler;
    private int maxSellChestsPlayer;
    private String defaultChestName;
    public boolean soldItemsLoggingPlayer;
    public boolean soldItemsLoggingConsole;
    private static ItemStack fillItem;
    private final Map<String, Integer> maxChests = new LinkedHashMap<String, Integer>();
    private Map<ChestLocation, Chest> loadedChests = new HashMap<ChestLocation, Chest>();
    private Map<UUID, ArrayList<Chest>> loadedChestsByPlayer = new HashMap<UUID, ArrayList<Chest>>();

    public ChestManager(AutoSellChests plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.soldItemsLoggingPlayer = Config.get().getBoolean("sold-items-logging-player");
        this.soldItemsLoggingConsole = Config.get().getBoolean("sold-items-logging-console");
        this.maxSellChestsPlayer = Config.get().getInt("max-sellchests.default");
        this.defaultChestName = Lang.formatColors(Config.get().getString("default-chest-name"), null);
        chestName = Lang.formatColors(Config.get().getString("sell-chest-item.name"), null);
        fillItem = ChestManager.createFillItem();
        this.loadChests();
        this.loadMaximumChests();
        this.loadAlreadyLoadedChests();
        if (Config.get().getBoolean("crafting.enabled")) {
            this.registerCraftingRecipe();
        }
        this.plugin.runTaskLater(this::startIntervalWhenReady, 1L);
    }

    private void startIntervalWhenReady() {
        this.scheduler = new MainScheduler(this.plugin);
    }

    public String getDefaultChestName() {
        return this.defaultChestName;
    }

    public Map<ChestLocation, Chest> getLoadedChests() {
        return this.loadedChests;
    }

    public ArrayList<Chest> getChestsByPlayer(UUID uuid) {
        return this.loadedChestsByPlayer.getOrDefault(uuid, new ArrayList<>());
    }

    public static ItemStack getFillItem() {
        return fillItem;
    }

    public Chest getChestByLocation(Location loc) {
        return this.loadedChests.get(new ChestLocation(loc));
    }

    public void updateChestInterval(Chest chest, int newIntervalID) {
        this.scheduler.updateChest(chest, newIntervalID);
    }

    public void loadChests(ChunkLoc chunkLoc) {
        for (ChestLocation location : this.loadedChests.keySet()) {
            if (!chunkLoc.contains(location)) continue;
            Chest chest = this.loadedChests.get(location);
            chest.setLoaded(true);
            this.plugin.getHologramManager().loadHologram(chest);
        }
    }

    public void unloadChests(ChunkLoc chunkLoc) {
        for (ChestLocation location : this.loadedChests.keySet()) {
            if (!chunkLoc.contains(location)) continue;
            Chest chest = this.loadedChests.get(location);
            chest.setLoaded(false);
        }
    }

    private void loadMaximumChests() {
        LinkedHashMap<String, Integer> maxChests = new LinkedHashMap<String, Integer>();
        for (String perm : Config.get().getConfigurationSection("max-sellchests.override").getKeys(false)) {
            String max = Config.get().getString("max-sellchests.override." + perm);
            try {
                maxChests.put(perm, Integer.parseInt(max));
            }
            catch (NumberFormatException e2) {
                Logger.warn("Failed to load max sellchest override like " + perm + "." + max + " inside config.yml");
            }
            catch (NullPointerException nullPointerException) {}
        }
        if (!this.maxChests.isEmpty()) {
            this.maxChests.clear();
        }
        if (!maxChests.isEmpty()) {
            ArrayList<Map.Entry<String, Integer>> list = new ArrayList<>(maxChests.entrySet());
            list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            list.forEach(e -> this.maxChests.put(e.getKey(), e.getValue()));
            Logger.info("Completed loading " + this.maxChests.size() + " max chest overrides");
        }
    }

    public int getMaxSell(Player player) {
        for (String perm : this.maxChests.keySet()) {
            if (!player.hasPermission("autosellchests.maxchests." + perm)) continue;
            return this.maxChests.get(perm);
        }
        return this.maxSellChestsPlayer;
    }

    public Chest getChestByID(int id) {
        for (Chest chest : this.loadedChests.values()) {
            if (chest.getId() != id) continue;
            return chest;
        }
        return null;
    }

    private void loadChests() {
        long start = System.currentTimeMillis();
        int i = 0;
        for (Chest chest : this.plugin.getDatabase().getAllChests()) {
            this.loadedChests.put(chest.getLocation(), chest);
            if (this.loadedChestsByPlayer.containsKey(chest.getOwner())) {
                List chests = this.loadedChestsByPlayer.get(chest.getOwner());
                chests.add(chest);
                this.loadedChestsByPlayer.put(chest.getOwner(), this.loadedChestsByPlayer.get(chest.getOwner()));
            } else {
                this.loadedChestsByPlayer.put(chest.getOwner(), new ArrayList<Chest>(Collections.singletonList(chest)));
            }
            ++i;
        }
        Logger.debug("Took " + (System.currentTimeMillis() - start) + "ms to load " + i + " sell chests from the database");
    }

    public void addChest(Location loc, @Nullable Chest original, ChestSettings settings, Player p) {
        if (original != null) {
            original.getLocation().addLocation(loc);
            this.plugin.getDatabase().setChest(original);
            this.loadedChests.remove(original.getLocation());
            this.loadedChests.put(original.getLocation(), original);
            this.plugin.getHologramManager().updateHologramLocation(original);
        } else {
            ChestLocation location = new ChestLocation(loc);
            this.plugin.getDatabase().addChest(location.toString(), p.getUniqueId().toString(), 0, settings);
            original = this.plugin.getDatabase().loadChest(location);
            original.setLoaded(true);
            this.scheduler.queueChest(original);
            this.plugin.getHologramManager().loadHologram(original);
            this.loadedChests.put(location, original);
            if (this.loadedChestsByPlayer.containsKey(p.getUniqueId())) {
                this.loadedChestsByPlayer.get(p.getUniqueId()).add(original);
            } else {
                this.loadedChestsByPlayer.put(p.getUniqueId(), new ArrayList<Chest>(Collections.singletonList(original)));
            }
        }
        Logger.debug("Added SellChest " + original.getId() + " for '" + String.valueOf(p.getUniqueId()) + "' on location: World '" + loc.getWorld().getName() + "', x" + loc.getBlockX() + ", y" + loc.getBlockY() + ", z" + loc.getBlockZ());
    }

    public void removeChest(ChestLocation loc) {
        Chest chest = this.loadedChests.get(loc);
        if (chest.getLocation().isDoubleChest()) {
            chest.getLocation().removeLocation(loc.getLeftLocation());
            this.plugin.getDatabase().setChest(chest);
            this.plugin.getHologramManager().updateHologramLocation(chest);
        } else {
            this.scheduler.removeFromQueue(chest);
            this.plugin.getDatabase().removeChest(loc.toString());
            this.loadedChests.remove(loc);
            if (this.loadedChestsByPlayer.get(chest.getOwner()).size() > 1) {
                this.loadedChestsByPlayer.get(chest.getOwner()).remove(chest);
            } else {
                this.loadedChestsByPlayer.remove(chest.getOwner());
            }
            this.plugin.getHologramManager().removeHologram(chest);
        }
        Logger.debug("Removed SellChest " + chest.getId() + " from '" + String.valueOf(chest.getOwner()) + "' on location: World '" + chest.getLocation().getLeftLocation().world + "', x" + chest.getLocation().getLeftLocation().x + ", y" + chest.getLocation().getLeftLocation().y + ", z" + chest.getLocation().getLeftLocation().z);
    }

    public void removeChest(Chest chest) {
        this.scheduler.removeFromQueue(chest);
        this.plugin.getDatabase().removeChest(chest.getLocation().toString());
        this.loadedChests.remove(chest.getLocation());
        if (this.loadedChestsByPlayer.containsKey(chest.getOwner())) {
            this.loadedChestsByPlayer.get(chest.getOwner()).remove(chest);
        } else {
            this.loadedChestsByPlayer.remove(chest.getOwner());
        }
        this.plugin.getHologramManager().removeHologram(chest);
        Logger.debug("Removed SellChest " + chest.getId() + " from '" + String.valueOf(chest.getOwner()) + "' on location: World '" + chest.getLocation().getLeftLocation().world + "', x" + chest.getLocation().getLeftLocation().x + ", y" + chest.getLocation().getLeftLocation().y + ", z" + chest.getLocation().getLeftLocation().z);
    }

    public ItemStack getChest(int amount) {
        return this.getChest((ChestSettings)null, amount);
    }

    public ItemStack getChest(Chest chest, int amount) {
        return this.getChest(chest == null ? null : chest.getSettings(), amount);
    }

    public ItemStack getChest(ChestSettings settings, int amount) {
        ItemStack item = new ItemStack(Material.CHEST, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(chestName);
        meta.setLore(Config.get().getStringList("sell-chest-item.lore").stream().map(s -> Lang.formatColors(s.replace("%interval%", TimeUtils.getReadableTime(this.getInterval(settings))), null)).collect(Collectors.toList()));
        int cmd = Config.get().getInt("sell-chest-item.CustomModelData");
        if (cmd != 0) {
            meta.setCustomModelData(Integer.valueOf(cmd));
        }
        meta.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "autosell"), PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(new NamespacedKey((Plugin)this.plugin, "autosell-data"), PersistentDataType.STRING, settings == null ? new ChestSettings().toString() : settings.toString());
        item.setItemMeta(meta);
        return item;
    }

    private long getInterval(ChestSettings settings) {
        return settings != null && UpgradeManager.intervalUpgrades ? UpgradeManager.getIntervals()[settings.interval] : UpgradeManager.getIntervals()[0];
    }

    private void saveChests() {
        Logger.debug("Saving '" + this.loadedChests.size() + "' chests...");
        this.plugin.getDatabase().saveChests(this.loadedChests.values());
    }

    private void registerCraftingRecipe() {
        HashMap<Material, Character> ingredients = new HashMap<Material, Character>();
        for (Object ingredient : Config.get().getStringList("crafting.custom-recipe.ingredients")) {
            try {
                if (!((String)ingredient).contains("::")) {
                    throw new InvalidIngredientException("Should be format of \"<symbol>::<material>\"");
                }
                String[] parts = ((String)ingredient).split("::");
                if (parts[0].length() > 1) {
                    throw new NullPointerException("Symbol should be length of 1, got " + parts[0].length() + " instead for " + parts[0]);
                }
                ingredients.put(Material.valueOf((String)parts[1]), Character.valueOf(parts[0].charAt(0)));
            }
            catch (Exception e) {
                Logger.warn("Failed to load crafting ingredient for " + (String)ingredient);
                if (e instanceof IllegalArgumentException) {
                    Logger.warn("Invalid recipe item type of '" + ((String)ingredient).split("::")[1] + "'");
                } else {
                    Logger.warn(e.getMessage());
                }
                return;
            }
        }
        try {
            List<String> pattern = Config.get().getStringList("crafting.custom-recipe.pattern").stream().map(s -> s.replace(" ", "")).toList();
            if (pattern.size() != 3 || pattern.stream().anyMatch(s -> s.length() != 3)) {
                throw new InvalidPatternException("Crafting pattern should be a grid of 3x3 symbols but got: " + Arrays.toString(pattern.toArray()));
            }
            Iterator<Character> ingredient = pattern.stream().flatMap(s -> s.chars().mapToObj(c -> (Character)(char)c)).toList().iterator();
            while (ingredient.hasNext()) {
                char symbol = ingredient.next();
                if (ingredients.containsValue(Character.valueOf(symbol))) continue;
                throw new InvalidPatternException("Crafting ingredient does not exist for symbol '" + symbol + "' in crafting pattern");
            }
            if (ingredients.containsKey(Material.AIR)) {
                pattern = pattern.stream().map(s -> s.replace(String.valueOf(ingredients.get(Material.AIR)), " ")).toList();
            }
            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey((Plugin)AutoSellChests.getInstance(), "sell_chest"), this.getChest(1));
            recipe.shape(pattern.toArray(new String[0]));
            for (Map.Entry ingredient2 : ingredients.entrySet()) {
                if (ingredient2.getKey() == Material.AIR) continue;
                recipe.setIngredient(((Character)ingredient2.getValue()).charValue(), (Material)ingredient2.getKey());
            }
            Bukkit.getServer().addRecipe((Recipe)recipe);
            Logger.info("Successfully registered custom sell chest crafting recipe");
        }
        catch (Exception e) {
            Logger.warn("Failed to load custom sell chest crafting recipe:");
            Logger.warn(e.getMessage());
        }
    }

    public void disable() {
        if (this.scheduler != null) {
            this.scheduler.stop();
        }
        this.scheduler.stop();
        this.saveChests();
        if (!this.loadedChests.isEmpty()) {
            this.loadedChests.clear();
        }
        if (!this.loadedChestsByPlayer.isEmpty()) {
            this.loadedChestsByPlayer.clear();
        }
    }

    public int getOwnedChests(Player p) {
        int i = 0;
        for (Chest chest : this.loadedChestsByPlayer.getOrDefault(p.getUniqueId(), new ArrayList<>())) {
            i += chest.getLocation().isDoubleChest() ? 2 : 1;
        }
        return i;
    }

    private static ItemStack createFillItem() {
        ItemStack item;
        ItemMeta meta;
        Material mat = Material.GRAY_STAINED_GLASS_PANE;
        String material = Config.get().getString("fill-item.material");
        if (material != null) {
            if (Material.getMaterial((String)material) != null) {
                mat = Material.getMaterial((String)material);
            } else {
                Logger.warn(String.format("Invalid material for fill-item %s, using default...", material));
            }
        }
        if ((meta = (item = new ItemStack(mat)).getItemMeta()) == null) {
            return item;
        }
        String name = Config.get().getString("fill-item.name");
        meta.setDisplayName(name != null ? ChatColor.translateAlternateColorCodes((char)'&', (String)name) : " ");
        int cmd = Config.get().getInt("fill-item.CustomModelData");
        if (cmd != 0) {
            meta.setCustomModelData(Integer.valueOf(cmd));
        }
        item.setItemMeta(meta);
        return item;
    }

    private void loadAlreadyLoadedChests() {
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                this.loadChests(new ChunkLoc(chunk));
            }
        }
    }
}

