package com.hazerengine.api;

import org.bukkit.entity.Player;

/**
 * Public economy API.
 */
public interface EconomyAPI {
    void addCoins(Player player, int amount);
    void addTokens(Player player, int amount);
    void addGems(Player player, int amount);
    int getCoins(Player player);
    int getTokens(Player player);
    int getGems(Player player);
}
