package com.desquad.engine.items;

import com.desquad.api.items.CustomItem;
import com.desquad.api.items.ItemRegistry;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryItemRegistry implements ItemRegistry {
    private final Map<String, CustomItem> items = new ConcurrentHashMap<>();

    @Override
    public void register(CustomItem item) {
        items.put(item.id(), item);
    }

    @Override
    public Optional<CustomItem> getItem(String id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Collection<CustomItem> allItems() {
        return items.values();
    }
}
