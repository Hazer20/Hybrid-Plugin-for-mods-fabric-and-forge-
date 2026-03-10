package com.hazerengine.core.performance;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    private final Map<String, CacheValue<?>> cache = new ConcurrentHashMap<>();

    public <T> void put(String key, T value, long ttlMillis) {
        cache.put(key, new CacheValue<>(value, Instant.now().toEpochMilli() + ttlMillis));
    }

    public Object get(String key) {
        CacheValue<?> cv = cache.get(key);
        if (cv == null || cv.expiresAt < Instant.now().toEpochMilli()) {
            cache.remove(key);
            return null;
        }
        return cv.value;
    }

    private record CacheValue<T>(T value, long expiresAt) {}
}
