package com.hazerengine.core.registry;

import com.hazerengine.core.api.Identifiable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BaseRegistry<T extends Identifiable> {
    protected final Map<String, T> data = new ConcurrentHashMap<>();

    public void register(T entry) {
        data.put(entry.id(), entry);
    }

    public Optional<T> get(String id) {
        return Optional.ofNullable(data.get(id));
    }

    public Collection<T> all() {
        return data.values();
    }

    public int size() {
        return data.size();
    }
}
