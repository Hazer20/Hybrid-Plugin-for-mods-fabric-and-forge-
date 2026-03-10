package com.desquad.api.mobs;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/** Базовый интерфейс кастомного моба. */
public interface CustomMob {
    String id();
    LivingEntity spawn(Location location);
}
