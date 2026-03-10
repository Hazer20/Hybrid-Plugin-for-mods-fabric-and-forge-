package com.desquad.api.economy;

import java.math.BigDecimal;
import java.util.UUID;

public interface EconomyService {
    BigDecimal getBalance(UUID playerId);
    void setBalance(UUID playerId, BigDecimal value);
    void give(UUID playerId, BigDecimal value);
    void take(UUID playerId, BigDecimal value);
}
