package com.desquad.api.items;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Представляет кастомный предмет ядра. */
public interface CustomItem {
    String id();
    ItemStack createStack();
    void onUse(Player player);
}
