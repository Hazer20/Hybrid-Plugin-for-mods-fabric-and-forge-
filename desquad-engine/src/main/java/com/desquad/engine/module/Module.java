package com.desquad.engine.module;

/** Контракт подключаемого модуля DESquadCore. */
public interface Module {
    String id();
    void enable();
    void disable();

    default boolean supportsHotReload() {
        return true;
    }
}
