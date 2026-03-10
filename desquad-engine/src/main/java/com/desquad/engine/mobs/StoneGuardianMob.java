package com.desquad.engine.mobs;

import com.desquad.api.mobs.CustomMob;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;

/** Пример кастомного моба stone_guardian. */
public final class StoneGuardianMob implements CustomMob {
    @Override public String id() { return "stone_guardian"; }

    @Override
    public LivingEntity spawn(Location location) {
        IronGolem entity = (IronGolem) location.getWorld().spawnEntity(location, EntityType.IRON_GOLEM);
        if (entity.getAttribute(Attribute.MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(60.0D);
        }
        entity.setHealth(60.0D);
        entity.setCustomName("§7Stone Guardian");
        return entity;
    }
}
