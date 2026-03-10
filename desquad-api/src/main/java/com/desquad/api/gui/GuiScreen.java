package com.desquad.api.gui;

import org.bukkit.entity.Player;

public interface GuiScreen {
    String id();
    void open(Player player);
}
