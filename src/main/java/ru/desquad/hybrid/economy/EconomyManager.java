package ru.desquad.hybrid.economy;

import ru.desquad.hybrid.DESHybridPlugin;
import ru.desquad.hybrid.storage.DataStorage;

import java.util.UUID;

public class EconomyManager {

    private final DESHybridPlugin plugin;
    private final DataStorage storage;

    public EconomyManager(DESHybridPlugin plugin, DataStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public double getBalance(UUID uuid) {
        return storage.getBalance(uuid);
    }

    public void setBalance(UUID uuid, double amount) {
        storage.setBalance(uuid, Math.max(0, amount));
    }

    public void add(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean withdraw(UUID uuid, double amount) {
        double bal = getBalance(uuid);
        if (bal < amount) {
            return false;
        }
        setBalance(uuid, bal - amount);
        return true;
    }

    public boolean transfer(UUID from, UUID to, double amount) {
        if (amount <= 0) {
            return false;
        }
        double tax = plugin.getConfig().getDouble("descoin.transfer-tax", 0.0);
        double withdrawAmount = amount + (amount * (tax / 100.0));
        if (!withdraw(from, withdrawAmount)) {
            return false;
        }
        add(to, amount);
        return true;
    }
}
