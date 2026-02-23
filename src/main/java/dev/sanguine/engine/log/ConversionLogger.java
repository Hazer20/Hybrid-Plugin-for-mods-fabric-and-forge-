package dev.sanguine.engine.log;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConversionLogger {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JavaPlugin plugin;
    private final Path file;

    public ConversionLogger(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = Path.of("plugins", "HybridConverter", "logs", "conversion.log");
        try {
            Files.createDirectories(file.getParent());
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
        String prefix = "[" + TS.format(LocalDateTime.now()) + "] [" + level + "] ";
        String body = prefix + message + System.lineSeparator();
        try {
            Files.writeString(file, body, StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
            if (throwable != null) {
                Files.writeString(file, throwable + System.lineSeparator(), StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.APPEND);
            }
        } catch (IOException ignored) {
            // keep server startup safe
        }

        if ("ERROR".equals(level)) {
            plugin.getLogger().severe(message);
        } else if ("WARN".equals(level)) {
            plugin.getLogger().warning(message);
        } else {
            plugin.getLogger().info(message);
        }
    }

    public Path file() {
        return file;
    }
}
