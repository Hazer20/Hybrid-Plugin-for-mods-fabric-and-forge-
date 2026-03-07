package dev.hazer.universe.events;

import dev.hazer.universe.FracturedUniverse;
import dev.hazer.universe.effects.PlayerInstabilityManager;
import dev.hazer.universe.portals.Fissure;
import dev.hazer.universe.portals.FissureManager;
import dev.hazer.universe.systems.EventPhase;
import dev.hazer.universe.systems.EventPhaseManager;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;

import java.util.concurrent.ThreadLocalRandom;

public class WorldEventListener implements Listener {
    private final FracturedUniverse plugin;
    private final EventPhaseManager phaseManager;
    private final FissureManager fissureManager;
    private final PlayerInstabilityManager instabilityManager;

    public WorldEventListener(FracturedUniverse plugin,
                              EventPhaseManager phaseManager,
                              FissureManager fissureManager,
                              PlayerInstabilityManager instabilityManager) {
        this.plugin = plugin;
        this.phaseManager = phaseManager;
        this.fissureManager = fissureManager;
        this.instabilityManager = instabilityManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        phaseManager.attachPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if (phaseManager.getPhase().ordinal() >= EventPhase.PHASE_2_PORTAL_FAILURE.ordinal()) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            player.sendMessage(ChatColor.DARK_GRAY + "[ERROR] Unknown dimension");
            player.sendMessage(ChatColor.DARK_GRAY + "[ERROR] Portal misalignment");
            player.sendMessage(ChatColor.DARK_GRAY + "[ERROR] Reality fragmentation");
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.4f);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage(ChatColor.RED + "⚠ Нарушилась целостность вселенной ⚠");
                player.getWorld().strikeLightningEffect(player.getLocation());
            }, 200L);
        }
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
        if (phaseManager.getPhase().ordinal() >= EventPhase.PHASE_2_PORTAL_FAILURE.ordinal()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }

        fissureManager.findNear(event.getTo(), 2.0).ifPresent(fissure -> {
            fissureManager.consumeFissureTravel(event.getPlayer(), fissure);
            instabilityManager.applyInstability(event.getPlayer());
        });

        if (phaseManager.getPhase().ordinal() >= EventPhase.PHASE_1_ANOMALIES.ordinal()) {
            maybeBlackChunk(event.getPlayer());
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (phaseManager.getPhase() == EventPhase.PHASE_1_ANOMALIES && event.getEntityType().isAlive()) {
            Entity entity = event.getEntity();
            if (ThreadLocalRandom.current().nextDouble() < 0.04) {
                Location lookAt = entity.getLocation().clone().add(6, 0, 6);
                entity.setRotation(entity.getLocation().setDirection(lookAt.toVector().subtract(entity.getLocation().toVector())).getYaw(), 0);
            }
        }
    }

    private void maybeBlackChunk(Player player) {
        int interval = plugin.getConfig().getInt("event.black-chunk-interval-seconds", 210);
        if (ThreadLocalRandom.current().nextInt(Math.max(1, interval)) != 0) {
            return;
        }

        Location location = player.getLocation();
        player.spawnParticle(Particle.SQUID_INK, location.clone().add(0, 1, 0), 40, 1.2, 1.5, 1.2, 0.02);
        player.playSound(location, Sound.ENTITY_WITHER_AMBIENT, 0.3f, 0.4f);
        player.sendActionBar(ChatColor.BLACK + "Chunk stream corrupted...");
    }
}
