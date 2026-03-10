package com.desquad.api.mobs;

import java.util.Collection;
import java.util.Optional;

/** Реестр кастомных мобов. */
public interface MobRegistry {
    void register(CustomMob mob);
    Optional<CustomMob> get(String id);
    Collection<CustomMob> all();
}
