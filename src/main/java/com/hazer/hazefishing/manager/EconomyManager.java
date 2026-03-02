package com.hazer.hazefishing.manager;

import com.hazer.hazefishing.HazerFishingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public final class EconomyManager {
    private final HazerFishingPlugin plugin;
    private Object economyProvider;
    private Method withdrawMethod;
    private Method depositMethod;
    private Method balanceMethod;

    public EconomyManager(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            var registration = Bukkit.getServicesManager().getRegistration(economyClass);
            if (registration == null) {
                plugin.getLogger().warning("Vault not found: economy disabled.");
                return;
            }
            economyProvider = registration.getProvider();
            withdrawMethod = economyClass.getMethod("withdrawPlayer", org.bukkit.OfflinePlayer.class, double.class);
            depositMethod = economyClass.getMethod("depositPlayer", org.bukkit.OfflinePlayer.class, double.class);
            balanceMethod = economyClass.getMethod("getBalance", org.bukkit.OfflinePlayer.class);
        } catch (Exception ex) {
            plugin.getLogger().warning("Cannot hook Vault economy: " + ex.getMessage());
        }
    }

    public boolean enabled() {
        return economyProvider != null;
    }

    public boolean withdraw(Player player, double amount) {
        if (!enabled()) return false;
        try {
            Object response = withdrawMethod.invoke(economyProvider, player, amount);
            Method success = response.getClass().getMethod("transactionSuccess");
            return (boolean) success.invoke(response);
        } catch (Exception e) {
            plugin.getLogger().warning("Withdraw failed: " + e.getMessage());
            return false;
        }
    }

    public void deposit(Player player, double amount) {
        if (!enabled()) return;
        try {
            depositMethod.invoke(economyProvider, player, amount);
        } catch (Exception e) {
            plugin.getLogger().warning("Deposit failed: " + e.getMessage());
        }
    }

    public double balance(Player player) {
        if (!enabled()) return 0.0;
        try {
            return ((Number) balanceMethod.invoke(economyProvider, player)).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }
}
