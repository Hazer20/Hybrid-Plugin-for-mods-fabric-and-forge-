package com.hazer.hazefishing.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class ParticleEngine {

    public void spawnSpiral(Player player, int tick) {
        Location base = player.getLocation().add(0, 1.2, 0);
        for (int i = 0; i < 16; i++) {
            double angle = (tick * 0.12) + (i * Math.PI / 8);
            double radius = 0.9;
            Vector offset = new Vector(Math.cos(angle) * radius, i * 0.03, Math.sin(angle) * radius);
            Location point = base.clone().add(offset);
            player.getWorld().spawnParticle(Particle.DUST, point, 1,
                    new Particle.DustOptions(org.bukkit.Color.fromRGB(
                            (int) (Math.sin(angle) * 127 + 128),
                            (int) (Math.sin(angle + 2) * 127 + 128),
                            (int) (Math.sin(angle + 4) * 127 + 128)),
                            1.2f));
        }
    }

    public void spawnBloom(Player player) {
        Location center = player.getLocation().add(0, 1.0, 0);
        player.getWorld().spawnParticle(Particle.END_ROD, center, 50, 0.8, 0.5, 0.8, 0.01);
        player.getWorld().spawnParticle(Particle.WAX_ON, center, 40, 1.0, 0.6, 1.0, 0.01);
    }
}
