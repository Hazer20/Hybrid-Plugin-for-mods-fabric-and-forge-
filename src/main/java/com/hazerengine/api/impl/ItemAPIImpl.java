package com.hazerengine.api.impl;

import com.hazerengine.api.ItemAPI;
import com.hazerengine.core.registry.ItemRegistry;
import com.hazerengine.items.CustomItem;

import java.util.Collection;
import java.util.Optional;

public class ItemAPIImpl implements ItemAPI {
    private final ItemRegistry registry;

    public ItemAPIImpl(ItemRegistry registry) {
        this.registry = registry;
    }

    @Override
    public CustomItem.Builder createItem(String id) {
        return CustomItem.builder(id);
    }

    @Override
    public void register(CustomItem item) {
        registry.register(item);
    }

    @Override
    public Optional<CustomItem> get(String id) {
        return registry.get(id);
    }

    @Override
    public Collection<CustomItem> all() {
        return registry.all();
    }
}
