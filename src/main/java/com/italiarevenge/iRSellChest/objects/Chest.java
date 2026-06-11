package com.italiarevenge.iRSellChest.objects;

import com.italiarevenge.iRSellChest.AutoSellChests;
import com.italiarevenge.iRSellChest.files.Config;
import com.italiarevenge.iRSellChest.files.Lang;
import com.italiarevenge.iRSellChest.managers.UpgradeManager;
import com.italiarevenge.iRSellChest.objects.ChestLocation;
import com.italiarevenge.iRSellChest.objects.ChestSettings;
import com.italiarevenge.iRSellChest.util.Logger;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Chest {

    private final int id;
    private String displayname;
    private final ChestLocation location;
    private final UUID owner;
    private boolean logging;
    private int itemsSold;
    private double income;
    private double claimAble;
    private int intervalUpgrade;
    private int multiplierUpgrade;
    private boolean enableHologram;
    private long interval;
    private long nextInterval;
    private double multiplier;
    private boolean loaded;

    public Chest(int id, String location, String owner, int itemsSold,
                 String income, String claimAble, String settings, String displayname) {
        this.id = id;
        this.location = new ChestLocation(location);
        this.owner = UUID.fromString(owner);
        this.itemsSold = itemsSold;
        this.income = parseDouble(income);
        this.claimAble = parseDouble(claimAble);
        this.logging = settings == null || settings.split("\\|")[0].equals("1");
        this.displayname = displayname == null
                ? Lang.formatColors(Config.get().getString("default-chest-name").replace("%id%", String.valueOf(id)), null)
                : displayname.replace("%id%", String.valueOf(id));
        this.intervalUpgrade = settings == null ? 0 : this.getIntervalLevel(settings);
        this.multiplierUpgrade = settings == null ? 0 : this.getMultiplierLevel(settings);
        this.enableHologram = settings == null || settings.split("\\|")[0].equals("1");
        this.interval = UpgradeManager.getIntervals()[UpgradeManager.intervalUpgrades ? this.intervalUpgrade : 0];
        this.multiplier = UpgradeManager.getMultipliers()[UpgradeManager.multiplierUpgrades ? this.multiplierUpgrade : 0];
    }

    public Chest(int id, ChestLocation location, Player owner, int itemsSold,
                 double income, double claimAble, boolean logging,
                 int intervalUpgrade, int multiplierUpgrade, String displayname) {
        this.id = id;
        this.location = location;
        this.owner = owner.getUniqueId();
        this.itemsSold = itemsSold;
        this.income = income;
        this.claimAble = claimAble;
        this.logging = logging;
        this.displayname = displayname.replace("%id%", String.valueOf(id));
        this.intervalUpgrade = intervalUpgrade;
        this.multiplierUpgrade = multiplierUpgrade;
        this.interval = UpgradeManager.getIntervals()[UpgradeManager.intervalUpgrades ? this.intervalUpgrade : 0];
        this.multiplier = UpgradeManager.getMultipliers()[UpgradeManager.multiplierUpgrades ? this.multiplierUpgrade : 0];
    }

    private double parseDouble(String raw) {
        if (raw == null || raw.isEmpty() || raw.equals("null")) return 0;
        // Legacy format: "VAULT;;12.5" or "VAULT;;12.5,,..."
        // New simple format: just a plain double string
        try {
            // Try plain double first
            return Double.parseDouble(raw);
        } catch (NumberFormatException ignored) {}
        // Try to extract numeric part from "TYPE;;VALUE" format
        try {
            String[] parts = raw.split(",,")[0].split(";;");
            if (parts.length >= 2) {
                return Double.parseDouble(parts[1]);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    private int getIntervalLevel(String settings) {
        try {
            return Integer.min(Integer.parseInt(settings.split("\\|")[1]), UpgradeManager.getIntervals().length - 1);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            Logger.warn("Failed to load interval level for '" + settings + "' for chest " + this.id + ", using default...");
            return 0;
        }
    }

    private int getMultiplierLevel(String settings) {
        try {
            return Integer.min(Integer.parseInt(settings.split("\\|")[2]), UpgradeManager.getMultipliers().length - 1);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            Logger.warn("Failed to load multiplier level for '" + settings + "' for chest " + this.id + ", using default...");
            return 0;
        }
    }

    public void addItemsSold(int itemsSold) {
        this.itemsSold += itemsSold;
    }

    public void addIncome(double amount) {
        this.income += amount;
    }

    public void addClaimAble(double price) {
        this.claimAble += price;
    }

    public void claim() {
        this.claimAble = 0;
    }

    public double getClaimAbleValue() {
        return this.claimAble;
    }

    public String getClaimAble(String message) {
        return AutoSellChests.getInstance().formatPrices(this.claimAble, message);
    }

    public String getClaimAbleRaw() {
        return String.valueOf(this.claimAble);
    }

    public void setNextInterval(long nextInterval) {
        this.nextInterval = nextInterval;
    }

    public int getId() {
        return this.id;
    }

    public ChestLocation getLocation() {
        return this.location;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public int getItemsSold() {
        return this.itemsSold;
    }

    public ChestSettings getSettings() {
        return new ChestSettings(this.logging, this.intervalUpgrade, this.multiplierUpgrade, this.enableHologram);
    }

    public String getIncomeRaw() {
        return String.valueOf(this.income);
    }

    public String getIncome(String message) {
        if (this.income == 0) {
            return AutoSellChests.getInstance().formatPrices(0, message);
        }
        return AutoSellChests.getInstance().formatPrices(this.income, message);
    }

    public long getNextInterval() {
        return this.nextInterval;
    }

    public long getInterval() {
        return this.interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public boolean isLogging() {
        return this.logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public boolean isHologram() {
        return this.enableHologram;
    }

    public void setHologram(boolean enabled) {
        this.enableHologram = enabled;
    }

    public int getIntervalUpgrade() {
        return this.intervalUpgrade;
    }

    public void setIntervalUpgrade(int upgrade) {
        this.intervalUpgrade = upgrade;
    }

    public double getMultiplier() {
        return this.multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public int getMultiplierUpgrade() {
        return this.multiplierUpgrade;
    }

    public void setMultiplierUpgrade(int upgrade) {
        this.multiplierUpgrade = upgrade;
    }

    public boolean isDoubleChest() {
        return this.location.isDoubleChest();
    }

    public String getName() {
        return this.displayname;
    }

    public void setName(String displayname) {
        this.displayname = displayname;
    }

    @Override
    public String toString() {
        return "{Id: " + this.id + ", Location: " + this.location + ", Owner: " + this.owner
                + ", ItemsSold: " + this.itemsSold + ", Income: " + this.income
                + ", Logging: " + this.logging + ", Interval upgrade: " + this.intervalUpgrade
                + ", Multiplier upgrade: " + this.multiplierUpgrade + "}";
    }
}
