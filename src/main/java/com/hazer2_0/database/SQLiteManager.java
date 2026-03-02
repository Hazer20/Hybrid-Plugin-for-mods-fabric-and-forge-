package com.hazer2_0.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class SQLiteManager {
    private final JavaPlugin plugin;
    private Connection connection;

    public SQLiteManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "hazer.db");
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS discs (
                            id TEXT PRIMARY KEY,
                            name TEXT NOT NULL,
                            file_path TEXT NOT NULL,
                            creator TEXT NOT NULL,
                            duration INTEGER NOT NULL,
                            created_at TEXT NOT NULL
                        )
                        """);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize SQLite", e);
        }
    }

    public void saveDisc(UUID id, String name, String filePath, String creator, int durationSec) {
        String sql = "INSERT INTO discs(id,name,file_path,creator,duration,created_at) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.setString(2, name);
            ps.setString(3, filePath);
            ps.setString(4, creator);
            ps.setInt(5, durationSec);
            ps.setString(6, Instant.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save disc", e);
        }
    }

    public Optional<DiscRow> findDisc(UUID id) {
        String sql = "SELECT * FROM discs WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new DiscRow(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getString("file_path"),
                        rs.getString("creator"),
                        rs.getInt("duration"),
                        rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("findDisc failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    public record DiscRow(UUID id, String name, String filePath, String creator, int duration, String createdAt) {}
}
