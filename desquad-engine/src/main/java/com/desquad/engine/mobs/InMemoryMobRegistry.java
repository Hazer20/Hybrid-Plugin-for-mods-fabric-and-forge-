package com.desquad.engine.mobs;

import com.desquad.api.mobs.CustomMob;
import com.desquad.api.mobs.MobRegistry;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Потокобезопасный реестр кастомных мобов. */
public final class InMemoryMobRegistry implements MobRegistry {
    private final Map<String, CustomMob> mobs = new ConcurrentHashMap<>();

    @Override
    public void register(CustomMob mob) {
        mobs.put(mob.id(), mob);
    }

    @Override
    public Optional<CustomMob> get(String id) {
        return Optional.ofNullable(mobs.get(id));
    }

    @Override
    public Collection<CustomMob> all() {
        return mobs.values();
    }
}
