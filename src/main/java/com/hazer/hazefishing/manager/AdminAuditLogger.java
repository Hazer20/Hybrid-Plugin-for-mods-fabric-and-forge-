package com.hazer.hazefishing.manager;

import com.hazer.hazefishing.HazerFishingPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public final class AdminAuditLogger {
    private final HazerFishingPlugin plugin;
    private final Path path;

    public AdminAuditLogger(HazerFishingPlugin plugin) {
        this.plugin = plugin;
        this.path = plugin.getDataFolder().toPath().resolve("admin-actions.log");
    }

    public void log(String actor, String command) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, Instant.now() + " | " + actor + " | " + command + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            plugin.getLogger().warning("Cannot write admin log: " + exception.getMessage());
        }
    }
}
