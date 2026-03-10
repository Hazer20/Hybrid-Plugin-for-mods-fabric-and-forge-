package com.hazerengine.abilities.impl;

import com.hazerengine.abilities.AbstractParticleAbility;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class EarthShatterAbility extends AbstractParticleAbility {
    @Override
    public void onUse(Player player) {
        burst(player, Particle.CRIT, 24);
    }
}
