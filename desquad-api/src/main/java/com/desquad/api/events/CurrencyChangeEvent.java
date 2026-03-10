package com.desquad.api.events;

import java.math.BigDecimal;
import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class CurrencyChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID playerId;
    private final BigDecimal oldValue;
    private final BigDecimal newValue;

    public CurrencyChangeEvent(UUID playerId, BigDecimal oldValue, BigDecimal newValue) {
        this.playerId = playerId;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public UUID getPlayerId() { return playerId; }
    public BigDecimal getOldValue() { return oldValue; }
    public BigDecimal getNewValue() { return newValue; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
