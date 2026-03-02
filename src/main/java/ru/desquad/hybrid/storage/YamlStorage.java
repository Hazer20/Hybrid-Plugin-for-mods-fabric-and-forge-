package ru.desquad.hybrid.storage;

import org.bukkit.configuration.file.YamlConfiguration;
import ru.desquad.hybrid.DESHybridPlugin;
import ru.desquad.hybrid.market.MarketListing;
import ru.desquad.hybrid.market.MarketTransaction;
import ru.desquad.hybrid.quest.PlayerQuestState;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlStorage implements DataStorage {

    private final DESHybridPlugin plugin;
    private File file;
    private YamlConfiguration data;

    public YamlStorage(DESHybridPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Не удалось создать data.yml", e);
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void close() {
        saveNow();
    }

    @Override
    public double getBalance(UUID uuid) {
        return data.getDouble("balances." + uuid, plugin.getConfig().getDouble("descoin.starting-balance", 0));
    }

    @Override
    public void setBalance(UUID uuid, double amount) {
        data.set("balances." + uuid, amount);
        saveNow();
    }

    @Override
    public Map<UUID, Double> getAllBalances() {
        Map<UUID, Double> balances = new HashMap<>();
        if (data.getConfigurationSection("balances") == null) {
            return balances;
        }
        for (String key : data.getConfigurationSection("balances").getKeys(false)) {
            balances.put(UUID.fromString(key), data.getDouble("balances." + key));
        }
        return balances;
    }

    @Override
    public PlayerQuestState getQuestState(UUID uuid) {
        String base = "quests." + uuid;
        if (!data.contains(base)) {
            return new PlayerQuestState();
        }
        PlayerQuestState state = new PlayerQuestState();
        state.setLastResetEpochDay(data.getLong(base + ".last-reset", 0));
        state.setCompletedToday(data.getInt(base + ".completed", 0));
        List<String> assigned = data.getStringList(base + ".assigned");
        state.setAssignedQuestIds(new ArrayList<>(assigned));
        return state;
    }

    @Override
    public void saveQuestState(UUID uuid, PlayerQuestState state) {
        String base = "quests." + uuid;
        data.set(base + ".last-reset", state.getLastResetEpochDay());
        data.set(base + ".completed", state.getCompletedToday());
        data.set(base + ".assigned", state.getAssignedQuestIds());
        saveNow();
    }

    @Override
    public List<MarketListing> getListings() {
        List<Map<?, ?>> list = data.getMapList("market.listings");
        List<MarketListing> output = new ArrayList<>();
        for (Map<?, ?> m : list) {
            output.add(MarketListing.fromMap(m));
        }
        return output;
    }

    @Override
    public void saveListings(List<MarketListing> listings) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (MarketListing listing : listings) {
            list.add(listing.toMap());
        }
        data.set("market.listings", list);
        saveNow();
    }

    @Override
    public List<MarketTransaction> getTransactions() {
        List<Map<?, ?>> list = data.getMapList("market.transactions");
        List<MarketTransaction> output = new ArrayList<>();
        for (Map<?, ?> m : list) {
            output.add(MarketTransaction.fromMap(m));
        }
        return output;
    }

    @Override
    public void saveTransactions(List<MarketTransaction> transactions) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (MarketTransaction transaction : transactions) {
            list.add(transaction.toMap());
        }
        data.set("market.transactions", list);
        saveNow();
    }

    @Override
    public boolean hasCraftedNpcToken(UUID uuid) {
        return data.getBoolean("npc-crafted." + uuid, false);
    }

    @Override
    public void setCraftedNpcToken(UUID uuid, boolean value) {
        data.set("npc-crafted." + uuid, value);
        saveNow();
    }

    private void saveNow() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить data.yml: " + e.getMessage());
        }
    }
}
