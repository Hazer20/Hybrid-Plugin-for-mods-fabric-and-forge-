package com.desquad.api.blocks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/** Базовый интерфейс кастомного блока DESquadCore. */
public interface CustomBlock {
    String id();
    void place(Player player, Location location);
}
