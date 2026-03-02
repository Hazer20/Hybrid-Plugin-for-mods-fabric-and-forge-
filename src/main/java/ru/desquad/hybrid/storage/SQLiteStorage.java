package ru.desquad.hybrid.storage;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import ru.desquad.hybrid.DESHybridPlugin;
import ru.desquad.hybrid.market.MarketListing;
import ru.desquad.hybrid.market.MarketTransaction;
import ru.desquad.hybrid.quest.PlayerQuestState;

import java.io.File;
import java.sql.*;
import java.util.*;

public class SQLiteStorage implements DataStorage {

    private final DESHybridPlugin plugin;
    private Connection connection;
    private final Gson gson = new Gson();

    public SQLiteStorage(DESHybridPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        try {
            File db = new File(plugin.getDataFolder(), plugin.getConfig().getString("storage.sqlite-file", "des_data.db"));
            db.getParentFile().mkdirs();
            connection = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
            Statement st = connection.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS balances(uuid TEXT PRIMARY KEY, amount REAL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS quests(uuid TEXT PRIMARY KEY, last_reset INTEGER, completed INTEGER, assigned TEXT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS market(key TEXT PRIMARY KEY, value TEXT)");
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка SQLite", e);
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    public double getBalance(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT amount FROM balances WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("amount");
        } catch (SQLException e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
        return plugin.getConfig().getDouble("descoin.starting-balance", 0);
    }

    @Override
    public void setBalance(UUID uuid, double amount) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO balances(uuid,amount) VALUES(?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setDouble(2, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
    }

    @Override
    public Map<UUID, Double> getAllBalances() {
        Map<UUID, Double> map = new HashMap<>();
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM balances");
            while (rs.next()) {
                map.put(UUID.fromString(rs.getString("uuid")), rs.getDouble("amount"));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
        return map;
    }

    @Override
    public PlayerQuestState getQuestState(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM quests WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PlayerQuestState state = new PlayerQuestState();
                state.setLastResetEpochDay(rs.getLong("last_reset"));
                state.setCompletedToday(rs.getInt("completed"));
                String raw = rs.getString("assigned");
                List<String> assigned = gson.fromJson(raw, List.class);
                state.setAssignedQuestIds(assigned != null ? assigned : new ArrayList<>());
                return state;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
        return new PlayerQuestState();
    }

    @Override
    public void saveQuestState(UUID uuid, PlayerQuestState state) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO quests(uuid,last_reset,completed,assigned) VALUES(?,?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setLong(2, state.getLastResetEpochDay());
            ps.setInt(3, state.getCompletedToday());
            ps.setString(4, gson.toJson(state.getAssignedQuestIds()));
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
    }

    @Override
    public List<MarketListing> getListings() {
        return decodeList(loadMarketValue("listings"), MarketListing[].class, MarketListing::new);
    }

    @Override
    public void saveListings(List<MarketListing> listings) {
        storeMarketValue("listings", gson.toJson(listings));
    }

    @Override
    public List<MarketTransaction> getTransactions() {
        return decodeList(loadMarketValue("transactions"), MarketTransaction[].class, MarketTransaction::new);
    }

    @Override
    public void saveTransactions(List<MarketTransaction> transactions) {
        storeMarketValue("transactions", gson.toJson(transactions));
    }

    private String loadMarketValue(String key) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT value FROM market WHERE key=?")) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("value");
        } catch (SQLException e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
        return "[]";
    }

    private void storeMarketValue(String key, String value) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO market(key,value) VALUES(?,?)")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
    }

    private <T> List<T> decodeList(String json, Class<T[]> type, java.util.function.Supplier<T> supplier) {
        T[] arr = gson.fromJson(json, type);
        if (arr == null) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(arr));
    }
}
