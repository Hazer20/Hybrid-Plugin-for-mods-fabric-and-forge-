package com.desquad.engine.gui;

import com.desquad.api.gui.GuiScreen;
import com.desquad.api.gui.GuiService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class SimpleGuiService implements GuiService {
    @Override
    public GuiScreen create(String id, String title, int size) {
        return new GuiScreen() {
            @Override public String id() { return id; }
            @Override public void open(Player player) {
                Inventory inv = Bukkit.createInventory(null, size, title);
                player.openInventory(inv);
            }
        };
    }
}
