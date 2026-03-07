package com.hazer.march8;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles all particles and cosmetic aura systems.
 */
public final class ParticleManager implements Listener {

    private final March8Plugin plugin;
    private final ConfigManager configManager;

    private final Map<UUID, Long> springAuraExpireMillis = new HashMap<>();
    private final Set<Particle> baseCelebrationParticles = EnumSet.noneOf(Particle.class);
    private final Set<Particle> springAuraParticles = EnumSet.noneOf(Particle.class);
    private final Set<Particle> swordAuraParticles = EnumSet.noneOf(Particle.class);

    private int springAuraInterval;
    private int swordAuraInterval;
    private BukkitTask springAuraTask;
    private BukkitTask swordAuraTask;

    public ParticleManager(@NotNull March8Plugin plugin, @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        reloadConfiguration();
        startTasks();
    }

    public void reloadConfiguration() {
        baseCelebrationParticles.clear();
        springAuraParticles.clear();
        swordAuraParticles.clear();

        parseParticleList(configManager.getStringList("effects.base-particles"), baseCelebrationParticles);
        parseParticleList(configManager.getStringList("effects.spring-aura.particles"), springAuraParticles);
        parseParticleList(configManager.getStringList("effects.sword-aura.particles"), swordAuraParticles);

        springAuraInterval = Math.max(1, configManager.getInt("effects.spring-aura.interval-ticks", 40));
        swordAuraInterval = Math.max(1, configManager.getInt("effects.sword-aura.interval-ticks", 10));

        restartTasks();
    }

    public void startCelebrationParticles(@NotNull Player player) {
        World world = player.getWorld();
        Location center = player.getLocation().clone().add(0, 1.1, 0);

        for (Particle particle : baseCelebrationParticles) {
            spawnRing(world, center, particle, 18, 1.1);
        }
        spawnPinkDustCloud(world, center.clone().add(0, 0.2, 0), 22, 1.2);
    }

    public void grantSpringAura(@NotNull Player player) {
        int seconds = Math.max(1, configManager.getInt("effects.spring-aura.duration-seconds", 300));
        long expiresAt = System.currentTimeMillis() + (seconds * 1000L);
        springAuraExpireMillis.put(player.getUniqueId(), expiresAt);
    }

    public boolean hasSpringAura(@NotNull Player player) {
        Long expire = springAuraExpireMillis.get(player.getUniqueId());
        return expire != null && expire > System.currentTimeMillis();
    }

    public void removeSpringAura(@NotNull Player player) {
        springAuraExpireMillis.remove(player.getUniqueId());
    }

    public void shutdown() {
        if (springAuraTask != null) {
            springAuraTask.cancel();
        }
        if (swordAuraTask != null) {
            swordAuraTask.cancel();
        }
        springAuraExpireMillis.clear();
    }

    private void restartTasks() {
        if (springAuraTask != null) {
            springAuraTask.cancel();
        }
        if (swordAuraTask != null) {
            swordAuraTask.cancel();
        }
        startTasks();
    }

    private void startTasks() {
        springAuraTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tickSpringAura, springAuraInterval, springAuraInterval);
        swordAuraTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tickSwordAura, swordAuraInterval, swordAuraInterval);
    }

    private void tickSpringAura() {
        long now = System.currentTimeMillis();

        springAuraExpireMillis.entrySet().removeIf(entry -> entry.getValue() <= now);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!hasSpringAura(player)) {
                continue;
            }
            Location base = player.getLocation().clone().add(0, 1.0, 0);
            World world = player.getWorld();

            for (Particle particle : springAuraParticles) {
                spawnOrbit(world, base, particle, 14, 1.2, 0.06);
            }
            spawnPinkDustCloud(world, base.clone().add(0, 0.2, 0), 10, 0.8);
        }
    }

    private void tickSwordAura() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!isHoldingSpringBlade(player)) {
                continue;
            }

            Location center = player.getLocation().clone().add(0, 1.0, 0);
            World world = player.getWorld();
            for (Particle particle : swordAuraParticles) {
                spawnRing(world, center, particle, 8, 0.75);
            }
            spawnPinkDustCloud(world, center, 4, 0.6);
        }
    }

    private boolean isHoldingSpringBlade(@NotNull Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();

        return isSpringBlade(main) || isSpringBlade(off);
    }

    private boolean isSpringBlade(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) {
            return false;
        }
        if (stack.getItemMeta() == null || stack.getItemMeta().displayName() == null) {
            return false;
        }
        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(stack.getItemMeta().displayName());
        return plain.toLowerCase().contains("клинок весны") || plain.toLowerCase().contains("spring");
    }

    private void spawnRing(@NotNull World world,
                           @NotNull Location center,
                           @NotNull Particle particle,
                           int points,
                           double radius) {
        for (int i = 0; i < points; i++) {
            double t = (Math.PI * 2D / points) * i;
            double x = Math.cos(t) * radius;
            double z = Math.sin(t) * radius;
            Location at = center.clone().add(x, 0, z);
            spawnParticle(world, particle, at);
        }
    }

    private void spawnOrbit(@NotNull World world,
                            @NotNull Location center,
                            @NotNull Particle particle,
                            int points,
                            double radius,
                            double verticalStep) {
        for (int i = 0; i < points; i++) {
            double t = (Math.PI * 2D / points) * i;
            double x = Math.cos(t) * radius;
            double z = Math.sin(t) * radius;
            double y = Math.sin(t * 2.0) * verticalStep;
            Location at = center.clone().add(x, y, z);
            spawnParticle(world, particle, at);
        }
    }

    private void spawnPinkDustCloud(@NotNull World world, @NotNull Location center, int count, double spread) {
        Particle.DustOptions pink = new Particle.DustOptions(Color.fromRGB(255, 120, 205), 1.4f);
        world.spawnParticle(Particle.DUST, center, count, spread, 0.3, spread, 0.01, pink);
    }

    private void spawnParticle(@NotNull World world, @NotNull Particle particle, @NotNull Location at) {
        switch (particle) {
            case DUST -> {
                Particle.DustOptions pink = new Particle.DustOptions(Color.fromRGB(255, 120, 205), 1.2f);
                world.spawnParticle(Particle.DUST, at, 1, 0.01, 0.01, 0.01, 0.0, pink);
            }
            case CHERRY_LEAVES, HEART, FIREWORK, GLOW -> world.spawnParticle(particle, at, 1, 0.01, 0.01, 0.01, 0.0);
            default -> world.spawnParticle(particle, at, 1, 0, 0, 0, 0);
        }
    }

    private void parseParticleList(@NotNull List<String> entries, @NotNull Set<Particle> target) {
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            String normalized = entry.trim().toUpperCase();
            if ("PINK_DUST".equals(normalized)) {
                target.add(Particle.DUST);
                continue;
            }

            try {
                target.add(Particle.valueOf(normalized));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Unknown particle in config: " + entry);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Aura is runtime-only and does not persist between restarts.
        removeSpringAura(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Keep map clean.
        removeSpringAura(event.getPlayer());
    }
}
