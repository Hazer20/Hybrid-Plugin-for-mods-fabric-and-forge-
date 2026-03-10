package com.desquad.api.items;

import java.util.Collection;
import java.util.Optional;

public interface ItemRegistry {
    void register(CustomItem item);
    Optional<CustomItem> getItem(String id);
    Collection<CustomItem> allItems();
}
