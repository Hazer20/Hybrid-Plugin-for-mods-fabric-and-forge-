package com.hazer.hazefishing.data;

import com.hazer.hazefishing.HazerFishingPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class DatabaseManager {
    private final HazerFishingPlugin plugin;
    private String jdbcUrl;
    private Properties props;

    public DatabaseManager(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String mode = plugin.getConfig().getString("database.mode", "sqlite");
        props = new Properties();
        if ("mysql".equalsIgnoreCase(mode)) {
            jdbcUrl = plugin.getConfig().getString("database.mysql.url");
            props.setProperty("user", plugin.getConfig().getString("database.mysql.username", ""));
            props.setProperty("password", plugin.getConfig().getString("database.mysql.password", ""));
        } else {
            File dbFile = new File(plugin.getDataFolder(), "hazefishing.db");
            jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        }
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
        if (props == null || props.isEmpty()) {
            return DriverManager.getConnection(jdbcUrl);
        }
        return DriverManager.getConnection(jdbcUrl, props);
    }

    public void shutdown() {
        // no pooled datasource to close
    }
}
