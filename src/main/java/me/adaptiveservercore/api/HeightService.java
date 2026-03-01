package me.adaptiveservercore.api;

import org.bukkit.entity.Player;

public interface HeightService {
    boolean setHeight(Player player, double height);

    double getHeight(Player player);

    boolean resetHeight(Player player);

    void registerHeightType(String name, double value);

    boolean isHeightAnimating(Player player);
}
