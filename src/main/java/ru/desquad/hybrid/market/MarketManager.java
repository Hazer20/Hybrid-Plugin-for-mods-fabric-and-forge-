package ru.desquad.hybrid.market;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.desquad.hybrid.DESHybridPlugin;
import ru.desquad.hybrid.economy.EconomyManager;
import ru.desquad.hybrid.storage.DataStorage;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class MarketManager {

    private final DESHybridPlugin plugin;
    private final DataStorage storage;
    private final EconomyManager economy;

    private final List<MarketListing> listings = new CopyOnWriteArrayList<>();
    private final List<MarketTransaction> transactions = new CopyOnWriteArrayList<>();

    public MarketManager(DESHybridPlugin plugin, DataStorage storage, EconomyManager economy) {
        this.plugin = plugin;
        this.storage = storage;
        this.economy = economy;

        listings.addAll(storage.getListings());
        transactions.addAll(storage.getTransactions());
    }

    public List<MarketListing> getListings() {
        return new ArrayList<>(listings);
    }

    public List<MarketListing> filter(Material material, String sellerName, Double minPrice, Double maxPrice) {
        return listings.stream()
                .filter(l -> material == null || l.getMaterial() == material)
                .filter(l -> sellerName == null || l.getSellerName().equalsIgnoreCase(sellerName))
                .filter(l -> minPrice == null || l.getPrice() >= minPrice)
                .filter(l -> maxPrice == null || l.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    public List<MarketTransaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public boolean addListing(Player seller, ItemStack item, double price) {
        long mine = listings.stream().filter(l -> l.getSeller().equals(seller.getUniqueId())).count();
        int max = plugin.getConfig().getInt("market.max-listings-per-player", 20);
        if (mine >= max) {
            seller.sendMessage("§cДостигнут лимит объявлений: " + max);
            return false;
        }
        if (item == null || item.getType().isAir()) {
            seller.sendMessage("§cНельзя выставить пустой предмет.");
            return false;
        }
        listings.add(new MarketListing(UUID.randomUUID().toString(), seller.getUniqueId(), seller.getName(), item.getType(), item.getAmount(), price));
        seller.getInventory().removeItem(item);
        persist();
        return true;
    }

    public boolean buy(Player buyer, String listingId) {
        MarketListing listing = listings.stream().filter(l -> l.getId().equals(listingId)).findFirst().orElse(null);
        if (listing == null) {
            buyer.sendMessage("§cЛот не найден.");
            return false;
        }
        if (!economy.withdraw(buyer.getUniqueId(), listing.getPrice())) {
            buyer.sendMessage("§cНедостаточно DESCoin.");
            return false;
        }

        double feePercent = plugin.getConfig().getDouble("market.fee-percent", 2.5);
        double sellerProfit = listing.getPrice() * (1 - feePercent / 100.0);
        economy.add(listing.getSeller(), sellerProfit);

        buyer.getInventory().addItem(listing.toItemStack());
        listings.remove(listing);

        transactions.add(new MarketTransaction(
                buyer.getName(), listing.getSellerName(), listing.getMaterial().name(), listing.getAmount(), listing.getPrice(), System.currentTimeMillis()
        ));

        int history = plugin.getConfig().getInt("market.history-size", 100);
        while (transactions.size() > history) {
            transactions.remove(0);
        }

        persist();
        buyer.sendMessage("§aПокупка успешна: " + listing.getMaterial().name() + " x" + listing.getAmount());
        return true;
    }

    public void shutdown() {
        persist();
    }

    private void persist() {
        storage.saveListings(getListings());
        storage.saveTransactions(getTransactions());
    }
}
