package com.hazerengine.abilities;

import org.bukkit.Particle;
import org.bukkit.entity.Player;

public abstract class AbstractParticleAbility implements Ability {
    protected void burst(Player player, Particle particle, int count) {
        player.getWorld().spawnParticle(particle, player.getLocation(), count, 0.4, 0.5, 0.4, 0.0);
    }
}
