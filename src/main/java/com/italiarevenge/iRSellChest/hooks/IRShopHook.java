package com.italiarevenge.iRSellChest.hooks;

import com.italiarevenge.iRShop.IRShop;
import com.italiarevenge.iRShop.model.Shop;
import com.italiarevenge.iRShop.model.ShopCategory;
import com.italiarevenge.iRShop.model.ShopItem;
import com.italiarevenge.iRShop.util.ItemMatcher;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IRShopHook {

    private static Economy economy;
    private static Permission permission;
    private static volatile List<ShopItem> sellableCache = null;

    public static boolean setupEconomy(JavaPlugin plugin) {
        RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public static void setupPermissions(JavaPlugin plugin) {
        RegisteredServiceProvider<Permission> rsp =
                plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) permission = rsp.getProvider();
    }

    public static double getPermissionBonus(OfflinePlayer player) {
        if (permission == null) return 0.0;
        if (permission.playerHas((String) null, player, "irshop.sell.2")) return 1.0;
        if (permission.playerHas((String) null, player, "irshop.sell.1.5")) return 0.5;
        if (permission.playerHas((String) null, player, "irshop.sell.1.25")) return 0.25;
        return 0.0;
    }

    public static Economy getEconomy() {
        return economy;
    }

    /** Call this when IR-Shop reloads its shop data so the cache is rebuilt on next sell. */
    public static void invalidateCache() {
        sellableCache = null;
    }

    public static double getSellValue(ItemStack[] items) {
        IRShop shop = IRShop.get();
        if (shop == null) return 0;

        List<ShopItem> sellableItems = getCachedSellableItems(shop);
        double total = 0;

        for (int i = 0; i < items.length; i++) {
            ItemStack stack = items[i];
            if (stack == null || stack.getType().isAir()) continue;

            for (ShopItem shopItem : sellableItems) {
                if (ItemMatcher.matchesStack(stack, shopItem)) {
                    total += shopItem.getSellPrice() * stack.getAmount();
                    items[i] = null;
                    break;
                }
            }
        }
        return total;
    }

    private static List<ShopItem> getCachedSellableItems(IRShop shop) {
        List<ShopItem> cached = sellableCache;
        if (cached != null) return cached;

        List<ShopItem> result = new ArrayList<>();
        for (Shop s : shop.getShopLoader().getShops().values()) {
            for (ShopCategory cat : s.getCategories()) {
                for (ShopItem item : cat.getItems()) {
                    if (item.isSellable()) {
                        result.add(item);
                        for (ShopItem variant : item.getVariants()) {
                            if (variant.isSellable()) result.add(variant);
                        }
                    }
                }
            }
        }
        cached = Collections.unmodifiableList(result);
        sellableCache = cached;
        return cached;
    }

    public static void deposit(OfflinePlayer player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    public static double getBalance(OfflinePlayer player) {
        return economy != null ? economy.getBalance(player) : 0;
    }

    public static boolean withdraw(OfflinePlayer player, double amount) {
        if (economy == null) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public static String formatPrice(double amount) {
        if (economy != null) {
            return economy.format(amount);
        }
        if (amount == Math.floor(amount)) {
            return String.format("%,.0f", amount);
        }
        return String.format("%,.2f", amount);
    }
}
