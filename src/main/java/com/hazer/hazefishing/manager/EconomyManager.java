package com.hazer.hazefishing.manager;

import com.hazer.hazefishing.HazerFishingPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyManager {
    private final HazerFishingPlugin plugin;
    private Economy economy;

    public EconomyManager(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
    }

    public boolean enabled() {
        return economy != null;
    }

    public boolean withdraw(Player player, double amount) {
        return enabled() && economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public void deposit(Player player, double amount) {
        if (enabled()) {
            economy.depositPlayer(player, amount);
        }
    }

    public double balance(Player player) {
        return enabled() ? economy.getBalance(player) : 0;
    }
}
