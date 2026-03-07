package dev.hazer.universe.portals;

import dev.hazer.universe.FracturedUniverse;
import dev.hazer.universe.systems.EventPhase;
import dev.hazer.universe.systems.EventPhaseManager;
import dev.hazer.universe.worlds.WorldManager;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
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
    private final Map<UUID, Location> returnPoints = new HashMap<>();
    private final Map<UUID, Long> teleportCooldown = new HashMap<>();

    public FissureManager(FracturedUniverse plugin, EventPhaseManager phaseManager, WorldManager worldManager) {
        this.plugin = plugin;
        this.phaseManager = phaseManager;
        this.worldManager = worldManager;

        Bukkit.getScheduler().runTaskTimer(plugin, this::tickFissures, 20L, 20L);
    }

    public void spawnSmallFissure(Location location) {
        long lifeSeconds = plugin.getConfig().getLong("разломы.малый.жизнь_сек", 600);
        Fissure fissure = new Fissure(UUID.randomUUID(), FissureType.SMALL, location.clone(), System.currentTimeMillis() + (lifeSeconds * 1000));
        activeFissures.put(fissure.getId(), fissure);
        renderFissure(fissure);
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "Разлом шепчет в координатах: " + format(location));
    }

    public void spawnGreatFissure(Location location) {
        Fissure fissure = new Fissure(UUID.randomUUID(), FissureType.GREAT, location.clone(), Long.MAX_VALUE);
        activeFissures.put(fissure.getId(), fissure);
        renderFissure(fissure);
        spawnMobsAround(location, plugin.getConfig().getInt("разломы.великий.волна_мобов", 8));
        Bukkit.broadcastMessage(ChatColor.RED + "Великий разлом раскрылся: " + format(location));
    }

    public void spawnLivingFissure(Location location) {
        Fissure fissure = new Fissure(UUID.randomUUID(), FissureType.LIVING, location.clone(), Long.MAX_VALUE);
        activeFissures.put(fissure.getId(), fissure);
        renderFissure(fissure);
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "Живой разлом скользит через пространство...");
    }

    public void spawnAmbientAnomaly() {
        if (!plugin.getConfig().getBoolean("разломы.включены", true)) {
            return;
        }
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

    public boolean repairNearestFissure(Location origin, double radius) {
        Optional<Fissure> fissure = findNear(origin, radius);
        if (fissure.isEmpty()) {
            return false;
        }
        clearFissureVisual(fissure.get());
        activeFissures.remove(fissure.get().getId());
        return true;
    }

    public void consumeFissureTravel(Player player, Fissure fissure) {
        long now = System.currentTimeMillis();
        Long cd = teleportCooldown.get(player.getUniqueId());
        if (cd != null && cd > now) {
            return;
        }
        teleportCooldown.put(player.getUniqueId(), now + 3000L);

        Location current = player.getLocation().clone();
        World overworld = Bukkit.getWorlds().getFirst();

        Location targetLoc;
        if (returnPoints.containsKey(player.getUniqueId()) && !current.getWorld().equals(overworld)) {
            // Возврат назад
            targetLoc = returnPoints.remove(player.getUniqueId());
        } else {
            returnPoints.put(player.getUniqueId(), current);
            World target = switch (fissure.getType()) {
                case SMALL -> Bukkit.getWorlds().get(ThreadLocalRandom.current().nextInt(Bukkit.getWorlds().size()));
                case GREAT -> worldManager.getLoadedBrokenOverworldOrFallback();
                case LIVING -> worldManager.getLoadedCyberDimensionOrFallback();
            };
            targetLoc = pickSafeLocation(target, target.getSpawnLocation());
        }

        player.teleport(targetLoc);
        player.playSound(targetLoc, Sound.BLOCK_PORTAL_TRAVEL, 1f, 0.65f);
        player.sendMessage(ChatColor.DARK_PURPLE + "Ты прошел через нестабильный разлом...");
    }

    public void cleanup() {
        for (Fissure fissure : activeFissures.values()) {
            clearFissureVisual(fissure);
        }
        activeFissures.clear();
        returnPoints.clear();
        teleportCooldown.clear();
    }

    private void tickFissures() {
        long now = System.currentTimeMillis();
        Iterator<Fissure> iterator = activeFissures.values().iterator();
        while (iterator.hasNext()) {
            Fissure fissure = iterator.next();
            if (fissure.getExpireAt() <= now) {
                clearFissureVisual(fissure);
                iterator.remove();
                continue;
            }

            Location loc = fissure.getLocation();
            loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc.clone().add(0, 1.2, 0), 15, 0.55, 1.2, 0.55, 0.01);

            if (fissure.getType() == FissureType.LIVING) {
                int moveInterval = plugin.getConfig().getInt("разломы.живой.шаг_перемещения_сек", 20);
                if (ThreadLocalRandom.current().nextInt(Math.max(1, moveInterval)) == 0) {
                    clearFissureVisual(fissure);
                    Location moved = loc.clone().add(ThreadLocalRandom.current().nextInt(-8, 9), 0, ThreadLocalRandom.current().nextInt(-8, 9));
                    moved.setY(moved.getWorld().getHighestBlockYAt(moved) + 1);
                    fissure.setLocation(moved);
                    renderFissure(fissure);
                }
            }
        }
    }

    private void renderFissure(Fissure fissure) {
        Location loc = fissure.getLocation();
        World world = loc.getWorld();

        world.playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 1.3f, 0.5f);
        world.spawnParticle(Particle.PORTAL, loc.clone().add(0, 1, 0), 120, 0.9, 1.3, 0.9, 0.15);
        world.spawnParticle(Particle.DRAGON_BREATH, loc.clone().add(0, 1, 0), 60, 0.6, 0.9, 0.6, 0.03);

        // Видимая рамка разлома
        placeFissureFrame(loc);
    }

    private void clearFissureVisual(Fissure fissure) {
        Location loc = fissure.getLocation();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block b = loc.clone().add(dx, 0, dz).getBlock();
                if (b.getType() == Material.CRYING_OBSIDIAN || b.getType() == Material.RESPAWN_ANCHOR) {
                    b.setType(Material.AIR, false);
                }
            }
        }
    }

    private void placeFissureFrame(Location loc) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block b = loc.clone().add(dx, 0, dz).getBlock();
                if (dx == 0 && dz == 0) {
                    b.setType(Material.RESPAWN_ANCHOR, false);
                } else {
                    b.setType(Material.CRYING_OBSIDIAN, false);
                }
            }
        }
    }

    private void spawnMobsAround(Location location, int amount) {
        World world = location.getWorld();
        for (int i = 0; i < amount; i++) {
            Location spawn = location.clone().add(ThreadLocalRandom.current().nextInt(-5, 6), 0, ThreadLocalRandom.current().nextInt(-5, 6));
            spawn = pickSafeLocation(world, spawn);
            LivingEntity entity = (LivingEntity) world.spawnEntity(spawn, EntityType.WITHER_SKELETON);
            entity.customName(Component.text("Fractured Echo"));
            entity.setCustomNameVisible(true);
        }
    }

    private Location pickSafeLocation(World world, Location base) {
        for (int i = 0; i < 20; i++) {
            int x = base.getBlockX() + ThreadLocalRandom.current().nextInt(-30, 31);
            int z = base.getBlockZ() + ThreadLocalRandom.current().nextInt(-30, 31);
            int y = world.getHighestBlockYAt(x, z);
            Location feet = new Location(world, x + 0.5, y + 1, z + 0.5);
            Material under = feet.clone().add(0, -1, 0).getBlock().getType();
            Material at = feet.getBlock().getType();
            Material head = feet.clone().add(0, 1, 0).getBlock().getType();

            boolean badFloor = under == Material.LAVA || under == Material.MAGMA_BLOCK || under == Material.CAMPFIRE || under == Material.SOUL_CAMPFIRE || under == Material.CACTUS;
            if (!badFloor && at.isAir() && head.isAir()) {
                return feet;
            }
        }
        Location fallback = world.getSpawnLocation().clone();
        fallback.setY(world.getHighestBlockYAt(fallback) + 1);
        return fallback;
    }

    private String format(Location location) {
        return location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
    }
}
