package com.hazer.hazefishing.manager;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.util.GradientEngine;
import com.hazer.hazefishing.util.ParticleEngine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RGBAnimationManager {
    private final HazerFishingPlugin plugin;
    private final GradientEngine gradientEngine;
    private final ParticleEngine particleEngine;
    private final Map<UUID, Integer> activeTicks = new ConcurrentHashMap<>();

    public RGBAnimationManager(HazerFishingPlugin plugin, GradientEngine gradientEngine, ParticleEngine particleEngine) {
        this.plugin = plugin;
        this.gradientEngine = gradientEngine;
        this.particleEngine = particleEngine;
    }

    public void triggerFishAnimation(Player player, String fishName) {
        activeTicks.put(player.getUniqueId(), 0);
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            Integer t = activeTicks.computeIfPresent(player.getUniqueId(), (u, tick) -> tick + 1);
            if (t == null || t > 80 || !player.isOnline()) {
                activeTicks.remove(player.getUniqueId());
                return;
            }
            Component title = gradientEngine.gradientText(fishName, t,
                    gradientEngine.oscillatingColor(t, 0),
                    gradientEngine.oscillatingColor(t, 20));
            player.showTitle(Title.title(title, Component.text("NFT catch!"),
                    Title.Times.times(Duration.ofMillis(150), Duration.ofMillis(300), Duration.ofMillis(150))));
            particleEngine.spawnSpiral(player, t);
            if (t % 10 == 0) {
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.8f, 0.6f + t * 0.01f);
            }
        }, 0L, 2L);
    }
}
