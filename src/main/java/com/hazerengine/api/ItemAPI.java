package com.hazerengine.api;

import com.hazerengine.items.CustomItem;

import java.util.Collection;
import java.util.Optional;

/**
 * Public item extension API for third-party plugins.
 */
public interface ItemAPI {
    CustomItem.Builder createItem(String id);
    void register(CustomItem item);
    Optional<CustomItem> get(String id);
    Collection<CustomItem> all();
}
