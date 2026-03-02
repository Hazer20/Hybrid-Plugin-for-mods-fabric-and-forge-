package ru.desquad.hybrid.storage;

import ru.desquad.hybrid.market.MarketListing;
import ru.desquad.hybrid.market.MarketTransaction;
import ru.desquad.hybrid.quest.PlayerQuestState;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DataStorage {
    void init();
    void close();

    double getBalance(UUID uuid);
    void setBalance(UUID uuid, double amount);
    Map<UUID, Double> getAllBalances();

    PlayerQuestState getQuestState(UUID uuid);
    void saveQuestState(UUID uuid, PlayerQuestState state);

    List<MarketListing> getListings();
    void saveListings(List<MarketListing> listings);

    List<MarketTransaction> getTransactions();
    void saveTransactions(List<MarketTransaction> transactions);
}
