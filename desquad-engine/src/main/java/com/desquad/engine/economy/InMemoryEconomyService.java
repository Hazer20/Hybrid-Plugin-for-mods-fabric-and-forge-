package com.desquad.engine.economy;

import com.desquad.api.economy.EconomyService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryEconomyService implements EconomyService {
    private final Map<UUID, BigDecimal> balances = new ConcurrentHashMap<>();

    @Override
    public BigDecimal getBalance(UUID playerId) {
        return balances.getOrDefault(playerId, BigDecimal.ZERO);
    }

    @Override
    public void setBalance(UUID playerId, BigDecimal value) {
        balances.put(playerId, value.max(BigDecimal.ZERO));
    }

    @Override
    public void give(UUID playerId, BigDecimal value) {
        setBalance(playerId, getBalance(playerId).add(value));
    }

    @Override
    public void take(UUID playerId, BigDecimal value) {
        setBalance(playerId, getBalance(playerId).subtract(value));
    }
}
