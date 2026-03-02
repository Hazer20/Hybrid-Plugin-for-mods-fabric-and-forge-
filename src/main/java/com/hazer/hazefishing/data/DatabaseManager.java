package com.hazer.hazefishing.data;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {
    private final HazerFishingPlugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String mode = plugin.getConfig().getString("database.mode", "sqlite");
        HikariConfig config = new HikariConfig();
        if ("mysql".equalsIgnoreCase(mode)) {
            config.setJdbcUrl(plugin.getConfig().getString("database.mysql.url"));
            config.setUsername(plugin.getConfig().getString("database.mysql.username"));
            config.setPassword(plugin.getConfig().getString("database.mysql.password"));
        } else {
            File dbFile = new File(plugin.getDataFolder(), "hazefishing.db");
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        }
        config.setMaximumPoolSize(10);
        config.setPoolName("HazeFishingPool");
        dataSource = new HikariDataSource(config);
        createTables();
    }

    private void createTables() {
        try (Connection connection = getConnection(); Statement st = connection.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS nft_rods (
                      rod_id VARCHAR(36) PRIMARY KEY,
                      owner VARCHAR(36) NOT NULL,
                      serial VARCHAR(20) UNIQUE,
                      created_at BIGINT,
                      tier VARCHAR(30),
                      level INT,
                      prestige INT,
                      seed BIGINT,
                      rarity_score DOUBLE,
                      integrity_hash VARCHAR(128)
                    )
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS nft_fish (
                      registry_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      fish_id VARCHAR(36) UNIQUE,
                      owner VARCHAR(36),
                      weight DOUBLE,
                      length DOUBLE,
                      rarity VARCHAR(20),
                      rarity_factor DOUBLE,
                      world_rank BIGINT,
                      genetic_code VARCHAR(300),
                      traits_json TEXT,
                      created_at BIGINT,
                      seed BIGINT
                    )
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS admin_logs (
                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                      actor VARCHAR(64),
                      command TEXT,
                      created_at BIGINT
                    )
                    """);
        } catch (SQLException exception) {
            plugin.getLogger().severe("DB init failed: " + exception.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
