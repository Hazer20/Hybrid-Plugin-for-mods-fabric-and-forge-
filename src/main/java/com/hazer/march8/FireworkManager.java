package com.hazer.march8;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Encapsulates all firework spawning logic.
 */
public final class FireworkManager {

    private final March8Plugin plugin;
    private final ConfigManager configManager;

    private int startCount;
    private int finalOverheadCount;

    public FireworkManager(@NotNull March8Plugin plugin, @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        reloadConfiguration();
    }

    public void reloadConfiguration() {
        this.startCount = Math.max(0, configManager.getInt("fireworks.on-start.count", 3));
        this.finalOverheadCount = Math.max(1, configManager.getInt("fireworks.final-overhead.count", 1));
    }

    public void spawnStartFireworks(@NotNull Player player) {
        for (int i = 0; i < startCount; i++) {
            double angle = (Math.PI * 2D / Math.max(1, startCount)) * i;
            double x = Math.cos(angle) * 2.0;
            double z = Math.sin(angle) * 2.0;
            Location base = player.getLocation().clone().add(x, 0.5, z);
            spawnCelebrationFirework(base, randomType(), randomPalette());
        }
    }

    public void spawnFinalFireworks(@NotNull Player player) {
        for (int i = 0; i < finalOverheadCount; i++) {
            Location overhead = player.getLocation().clone().add(0, 2.5 + i, 0);
            spawnCelebrationFirework(overhead, FireworkEffect.Type.BALL_LARGE, List.of(Color.FUCHSIA, Color.PURPLE, Color.MAROON));
        }
    }

    public void spawnImpactFirework(@NotNull Location location) {
        spawnCelebrationFirework(location.clone().add(0, 0.3, 0), FireworkEffect.Type.BURST, List.of(Color.PINK, Color.WHITE, Color.RED));
    }

    public void spawnCelebrationFirework(@NotNull Location at, @NotNull FireworkEffect.Type type, @NotNull List<Color> colors) {
        World world = at.getWorld();
        if (world == null) {
            return;
        }

        Firework firework = (Firework) world.spawnEntity(at, EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Builder effectBuilder = FireworkEffect.builder()
                .with(type)
                .flicker(true)
                .trail(true);

        for (Color color : colors) {
            effectBuilder.withColor(color);
        }

        meta.clearEffects();
        meta.addEffect(effectBuilder.build());
        meta.setPower(1);
        firework.setFireworkMeta(meta);

        // Auto-detonate quickly for visual focus around player.
        plugin.getServer().getScheduler().runTaskLater(plugin, firework::detonate, 14L);
    }

    private FireworkEffect.Type randomType() {
        FireworkEffect.Type[] types = FireworkEffect.Type.values();
        return types[ThreadLocalRandom.current().nextInt(types.length)];
    }

    private List<Color> randomPalette() {
        List<List<Color>> palettes = List.of(
                List.of(Color.PINK, Color.FUCHSIA, Color.WHITE),
                List.of(Color.RED, Color.MAROON, Color.WHITE),
                List.of(Color.PURPLE, Color.FUCHSIA, Color.NAVY),
                List.of(Color.ORANGE, Color.YELLOW, Color.WHITE)
        );
        return palettes.get(ThreadLocalRandom.current().nextInt(palettes.size()));
    }
}
