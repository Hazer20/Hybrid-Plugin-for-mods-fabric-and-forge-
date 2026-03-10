package com.desquad.engine.tick;

/**
 * Контракт подсистемы, работающей в основном серверном тик-цикле.
 */
public interface TickSubsystem {
    String id();
    void onTick(long currentTick);
}
