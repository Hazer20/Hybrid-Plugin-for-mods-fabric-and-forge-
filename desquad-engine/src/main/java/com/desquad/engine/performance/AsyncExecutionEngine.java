package com.desquad.engine.performance;

import com.desquad.api.performance.PerformanceService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Асинхронный движок задач ядра, распределяющий load по пулам потоков.
 */
public final class AsyncExecutionEngine implements PerformanceService {
    private final Map<String, ExecutorService> pools = new LinkedHashMap<>();
    private final Map<String, Integer> sizes = new LinkedHashMap<>();

    public AsyncExecutionEngine(int workerThreads) {
        int base = Math.max(2, workerThreads);
        registerPool("ticks", base);
        registerPool("ai", Math.max(2, base / 2));
        registerPool("items", Math.max(2, base / 2));
        registerPool("resourcepack", Math.max(2, base / 2));
    }

    private void registerPool(String pool, int size) {
        sizes.put(pool, size);
        pools.put(pool, Executors.newFixedThreadPool(size, namedFactory(pool)));
    }

    private ThreadFactory namedFactory(String pool) {
        AtomicInteger index = new AtomicInteger(1);
        return task -> {
            Thread t = new Thread(task, "desquad-" + pool + "-" + index.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
    }

    @Override
    public <T> CompletableFuture<T> submit(String pool, Callable<T> task) {
        ExecutorService executor = pools.getOrDefault(pool, pools.get("ticks"));
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> submit(String pool, Runnable task) {
        ExecutorService executor = pools.getOrDefault(pool, pools.get("ticks"));
        return CompletableFuture.runAsync(task, executor);
    }

    @Override
    public Map<String, Integer> poolSizes() {
        return Map.copyOf(sizes);
    }

    @Override
    public void shutdown() {
        pools.values().forEach(ExecutorService::shutdownNow);
        pools.clear();
        sizes.clear();
    }
}
