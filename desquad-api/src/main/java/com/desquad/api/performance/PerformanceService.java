package com.desquad.api.performance;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис высокопроизводительного выполнения тяжелых задач ядра.
 * Используется для асинхронной обработки AI, тиков, моделей и resourcepack-задач.
 */
public interface PerformanceService {
    <T> CompletableFuture<T> submit(String pool, Callable<T> task);
    CompletableFuture<Void> submit(String pool, Runnable task);
    Map<String, Integer> poolSizes();
    void shutdown();
}
