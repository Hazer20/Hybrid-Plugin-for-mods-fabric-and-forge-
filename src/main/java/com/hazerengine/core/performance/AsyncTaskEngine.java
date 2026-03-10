package com.hazerengine.core.performance;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncTaskEngine {
    private final ExecutorService pool;

    public AsyncTaskEngine(JavaPlugin plugin, int threads) {
        this.pool = Executors.newFixedThreadPool(threads, r -> new Thread(r, plugin.getName() + "-async"));
    }

    public void runAsync(Runnable runnable) { pool.submit(runnable); }
    public void shutdown() { pool.shutdownNow(); }
}
