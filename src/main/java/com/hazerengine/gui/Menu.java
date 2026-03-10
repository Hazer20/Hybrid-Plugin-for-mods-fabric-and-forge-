package com.hazerengine.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface Menu {
    String id();
    Inventory create(Player player, int page);
}
