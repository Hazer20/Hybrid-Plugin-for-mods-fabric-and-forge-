package com.hazerengine.economy;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyAPI implements com.hazerengine.api.EconomyAPI {
    private final Map<UUID, Integer> coins = new HashMap<>();
    private final Map<UUID, Integer> tokens = new HashMap<>();
    private final Map<UUID, Integer> gems = new HashMap<>();

    @Override
    public void addCoins(Player player, int amount) { coins.merge(player.getUniqueId(), amount, Integer::sum); }
    @Override
    public void addTokens(Player player, int amount) { tokens.merge(player.getUniqueId(), amount, Integer::sum); }
    @Override
    public void addGems(Player player, int amount) { gems.merge(player.getUniqueId(), amount, Integer::sum); }
    @Override
    public int getCoins(Player player) { return coins.getOrDefault(player.getUniqueId(), 0); }
    @Override
    public int getTokens(Player player) { return tokens.getOrDefault(player.getUniqueId(), 0); }
    @Override
    public int getGems(Player player) { return gems.getOrDefault(player.getUniqueId(), 0); }
}
