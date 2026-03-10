package com.hazerengine.economy;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyAPI {
    private final Map<UUID, Integer> coins = new HashMap<>();
    private final Map<UUID, Integer> tokens = new HashMap<>();
    private final Map<UUID, Integer> gems = new HashMap<>();

    public void addCoins(Player player, int amount) { coins.merge(player.getUniqueId(), amount, Integer::sum); }
    public void addTokens(Player player, int amount) { tokens.merge(player.getUniqueId(), amount, Integer::sum); }
    public void addGems(Player player, int amount) { gems.merge(player.getUniqueId(), amount, Integer::sum); }
}
