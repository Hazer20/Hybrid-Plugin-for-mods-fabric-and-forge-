package com.hazer2_0.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncExecutor {
    private final ExecutorService ioExecutor = Executors.newFixedThreadPool(4);
    private final JavaPlugin plugin;

    public AsyncExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, ioExecutor)
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Async task failed: " + ex.getMessage());
                    return null;
                });
    }

    public void shutdown() {
        ioExecutor.shutdownNow();
    }
}
