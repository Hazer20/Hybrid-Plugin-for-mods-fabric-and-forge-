package com.hazerengine.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Bukkit;
import org.bukkit.inventory.Inventory;

public class PaginatedMenu implements Menu {
    @Override public String id(){return "paginated";}
    @Override public Inventory create(Player player, int page){return Bukkit.createInventory(null,54,"Hazer Menu " + page);}
}
