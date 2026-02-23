package dev.sanguine.engine.log;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ConversionLogger {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JavaPlugin plugin;
    private final Path file;

    public ConversionLogger(JavaPlugin plugin, Path logsDir) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.file = logsDir.resolve("conversion.log");
        try {
            Files.createDirectories(logsDir);
        } catch (IOException e) {
            plugin.getLogger().warning("Cannot create conversion log directory: " + e.getMessage());
        }
    }

    public synchronized void info(String message) {
        write("INFO", message, null);
    }

    public synchronized void warn(String message) {
        write("WARN", message, null);
    }

    public synchronized void error(String message, Throwable error) {
        write("ERROR", message, error);
    }

    private void write(String level, String message, Throwable throwable) {
        String body = "[" + TS.format(LocalDateTime.now()) + "] [" + level + "] " + message + System.lineSeparator();
        try {
            Files.writeString(file, body, StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            if (throwable != null) {
                Files.writeString(file, throwable + System.lineSeparator(), StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.APPEND);
            }
        } catch (IOException ignored) {
        }

        switch (level) {
            case "ERROR" -> plugin.getLogger().severe(message);
            case "WARN" -> plugin.getLogger().warning(message);
            default -> plugin.getLogger().info(message);
        }
    }

    public Path file() {
        return file;
    }
}
