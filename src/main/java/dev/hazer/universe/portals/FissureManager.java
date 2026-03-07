package dev.hazer.universe.portals;

import dev.hazer.universe.FracturedUniverse;
import dev.hazer.universe.systems.EventPhase;
import dev.hazer.universe.systems.EventPhaseManager;
import dev.hazer.universe.worlds.WorldManager;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FissureManager {
    private final FracturedUniverse plugin;
    private final EventPhaseManager phaseManager;
    private final WorldManager worldManager;
    private final Map<UUID, Fissure> activeFissures = new HashMap<>();

    public FissureManager(FracturedUniverse plugin, EventPhaseManager phaseManager, WorldManager worldManager) {
        this.plugin = plugin;
        this.phaseManager = phaseManager;
        this.worldManager = worldManager;

        Bukkit.getScheduler().runTaskTimer(plugin, this::tickFissures, 20L, 20L);
    }

    public void spawnSmallFissure(Location location) {
        long lifeSeconds = plugin.getConfig().getLong("fissures.small.lifetime-seconds", 600);
        Fissure fissure = new Fissure(UUID.randomUUID(), FissureType.SMALL, location.clone(), System.currentTimeMillis() + (lifeSeconds * 1000));
        activeFissures.put(fissure.getId(), fissure);
        renderFissure(fissure);
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "Разлом шепчет в координатах: " + format(location));
    }

    public void spawnGreatFissure(Location location) {
        Fissure fissure = new Fissure(UUID.randomUUID(), FissureType.GREAT, location.clone(), Long.MAX_VALUE);
        activeFissures.put(fissure.getId(), fissure);
        renderFissure(fissure);
        spawnMobsAround(location, plugin.getConfig().getInt("fissures.great.mob-wave-size", 6));
        Bukkit.broadcastMessage(ChatColor.RED + "Великий разлом раскрылся: " + format(location));
    }

    public void spawnLivingFissure(Location location) {
        Fissure fissure = new Fissure(UUID.randomUUID(), FissureType.LIVING, location.clone(), Long.MAX_VALUE);
        activeFissures.put(fissure.getId(), fissure);
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Живой разлом скользит через пространство...");
    }

    public void spawnAmbientAnomaly() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spawnParticle(Particle.PORTAL, player.getLocation().add(0, 2.2, 0), 24, 0.8, 1.0, 0.8, 0.02);
            if (ThreadLocalRandom.current().nextDouble() < 0.08) {
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 0.6f, 0.4f);
            }
        }
    }

    public void triggerStarfall() {
        World world = Bukkit.getWorlds().getFirst();
        Location center = world.getSpawnLocation().clone().add(ThreadLocalRandom.current().nextInt(-250, 250), 0, ThreadLocalRandom.current().nextInt(-250, 250));
        center.setY(world.getHighestBlockYAt(center) + 1);

        world.createExplosion(center, 7.5f, false, false);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 1);
        world.spawnParticle(Particle.END_ROD, center, 180, 8, 4, 8, 0.12);
        Bukkit.broadcastMessage(ChatColor.GOLD + "Небо треснуло. Пала звезда в " + format(center));

        spawnGreatFissure(center.clone().add(0, 1, 0));
        phaseManager.setPhase(EventPhase.PHASE_4_FRACTURE_WAR);
    }

    public Optional<Fissure> findNear(Location location, double radius) {
        return activeFissures.values().stream()
                .filter(f -> f.getLocation().getWorld().equals(location.getWorld()))
                .filter(f -> f.getLocation().distanceSquared(location) <= radius * radius)
                .findFirst();
    }

    public void consumeFissureTravel(Player player, Fissure fissure) {
        World target = switch (fissure.getType()) {
            case SMALL -> Bukkit.getWorlds().get(ThreadLocalRandom.current().nextInt(Bukkit.getWorlds().size()));
            case GREAT -> worldManager.getOrCreateBrokenOverworld();
            case LIVING -> worldManager.getOrCreateCyberDimension();
        };

        Location targetLoc = target.getSpawnLocation().clone().add(ThreadLocalRandom.current().nextInt(-30, 31), 0, ThreadLocalRandom.current().nextInt(-30, 31));
        targetLoc.setY(target.getHighestBlockYAt(targetLoc) + 1);

        player.teleport(targetLoc);
        player.playSound(targetLoc, Sound.BLOCK_PORTAL_TRAVEL, 1f, 0.65f);
        player.sendMessage(ChatColor.DARK_PURPLE + "Ты прошел через нестабильный разлом...");
    }

    public void cleanup() {
        activeFissures.clear();
    }

    private void tickFissures() {
        long now = System.currentTimeMillis();
        Iterator<Fissure> iterator = activeFissures.values().iterator();
        while (iterator.hasNext()) {
            Fissure fissure = iterator.next();
            if (fissure.getExpireAt() <= now) {
                iterator.remove();
                continue;
            }

            Location loc = fissure.getLocation();
            loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc.clone().add(0, 1.2, 0), 15, 0.55, 1.2, 0.55, 0.01);

            if (fissure.getType() == FissureType.LIVING) {
                int moveInterval = plugin.getConfig().getInt("fissures.living.move-interval-seconds", 20);
                if (ThreadLocalRandom.current().nextInt(Math.max(1, moveInterval)) == 0) {
                    Location moved = loc.clone().add(ThreadLocalRandom.current().nextInt(-8, 9), 0, ThreadLocalRandom.current().nextInt(-8, 9));
                    moved.setY(moved.getWorld().getHighestBlockYAt(moved) + 1);
                    fissure.setLocation(moved);
                }
            }
        }
    }

    private void renderFissure(Fissure fissure) {
        Location loc = fissure.getLocation();
        World world = loc.getWorld();
        world.playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 1.3f, 0.5f);
        world.spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 80, 0.8, 1.2, 0.8, 0.15);
        world.spawnParticle(Particle.DRAGON_BREATH, loc.clone().add(0, 1, 0), 45, 0.5, 0.8, 0.5, 0.03);
    }

    private void spawnMobsAround(Location location, int amount) {
        World world = location.getWorld();
        for (int i = 0; i < amount; i++) {
            Location spawn = location.clone().add(ThreadLocalRandom.current().nextInt(-5, 6), 0, ThreadLocalRandom.current().nextInt(-5, 6));
            spawn.setY(world.getHighestBlockYAt(spawn) + 1);
            LivingEntity entity = (LivingEntity) world.spawnEntity(spawn, EntityType.WITHER_SKELETON);
            entity.customName(Component.text("Fractured Echo"));
            entity.setCustomNameVisible(true);
        }
    }

    private String format(Location location) {
        return location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
    }
}
