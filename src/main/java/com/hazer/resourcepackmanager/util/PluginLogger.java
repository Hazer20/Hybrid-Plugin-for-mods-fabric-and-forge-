package com.hazer.resourcepackmanager.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thin wrapper around Bukkit logger that provides a consistent structured prefix.
 * <p>
 * We keep logging in one place to simplify future migration to structured logging
 * and to make unit testing of message-producing code easier.
 */
public final class PluginLogger {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Logger logger;
    private final boolean debugEnabled;

    /**
     * Constructs logger wrapper.
     *
     * @param plugin plugin instance
     * @param debugEnabled enables debug log entries
     */
    public PluginLogger(final JavaPlugin plugin, final boolean debugEnabled) {
        Objects.requireNonNull(plugin, "plugin");
        this.logger = plugin.getLogger();
        this.debugEnabled = debugEnabled;
    }

    /**
     * Logs info-level message.
     *
     * @param message text
     */
    public void info(final String message) {
        logger.info(prefix("INFO", message));
    }

    /**
     * Logs warning-level message.
     *
     * @param message text
     */
    public void warn(final String message) {
        logger.warning(prefix("WARN", message));
    }

    /**
     * Logs severe-level message.
     *
     * @param message text
     */
    public void error(final String message) {
        logger.severe(prefix("ERROR", message));
    }

    /**
     * Logs severe-level message with exception stack trace.
     *
     * @param message text
     * @param throwable exception
     */
    public void error(final String message, final Throwable throwable) {
        logger.log(Level.SEVERE, prefix("ERROR", message), throwable);
    }

    /**
     * Logs debug-level message if enabled.
     *
     * @param message text
     */
    public void debug(final String message) {
        if (debugEnabled) {
            logger.info(prefix("DEBUG", message));
        }
    }

    private String prefix(final String level, final String message) {
        return "[" + TS.format(LocalDateTime.now()) + "] [" + level + "] " + message;
    }
}
