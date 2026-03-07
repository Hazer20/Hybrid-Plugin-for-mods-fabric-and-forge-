package dev.hazer.universe.systems;

import dev.hazer.universe.FracturedUniverse;
import dev.hazer.universe.effects.PlayerInstabilityManager;
import dev.hazer.universe.portals.FissureManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.concurrent.ThreadLocalRandom;

public class EventScheduler {
    private final FracturedUniverse plugin;
    private final EventPhaseManager phaseManager;
    private final FissureManager fissureManager;
    private final PlayerInstabilityManager instabilityManager;
    private final ArgSignalService argSignalService;

    public EventScheduler(FracturedUniverse plugin,
                          EventPhaseManager phaseManager,
                          FissureManager fissureManager,
                          PlayerInstabilityManager instabilityManager,
                          ArgSignalService argSignalService) {
        this.plugin = plugin;
        this.phaseManager = phaseManager;
        this.fissureManager = fissureManager;
        this.instabilityManager = instabilityManager;
        this.argSignalService = argSignalService;
    }

    public void bootstrap() {
        Bukkit.getScheduler().runTaskTimer(plugin, phaseManager::tick, 20L, 20L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, argSignalService::emitServerLogSignal, 20L * 45, 20L * 90);
        Bukkit.getScheduler().runTaskTimer(plugin, argSignalService::playAnomalyPulse, 20L * 20, 20L * 30);
        Bukkit.getScheduler().runTaskTimer(plugin, instabilityManager::tickInstability, 20L, 20L * 5L);

        int anomalyInterval = plugin.getConfig().getInt("event.auto-anomaly-interval-seconds", 180);
        int fissureInterval = plugin.getConfig().getInt("event.auto-fissure-interval-seconds", 420);

        Bukkit.getScheduler().runTaskTimer(plugin, this::spawnAmbientAnomaly, 20L * 40, anomalyInterval * 20L);
        Bukkit.getScheduler().runTaskTimer(plugin, this::spawnRandomFissure, 20L * 60, fissureInterval * 20L);
    }

    public void scheduleStarfall() {
        int min = plugin.getConfig().getInt("event.starfall.min-minutes", 5);
        int max = plugin.getConfig().getInt("event.starfall.max-minutes", 20);
        int delayMinutes = ThreadLocalRandom.current().nextInt(min, max + 1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            phaseManager.setPhase(EventPhase.PHASE_3_STARFALL);
            fissureManager.triggerStarfall();
        }, delayMinutes * 60L * 20L);
    }

    public void shutdown() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    private void spawnAmbientAnomaly() {
        if (phaseManager.getPhase().ordinal() < EventPhase.PHASE_1_ANOMALIES.ordinal()) {
            return;
        }
        fissureManager.spawnAmbientAnomaly();
    }

    private void spawnRandomFissure() {
        if (phaseManager.getPhase().ordinal() < EventPhase.PHASE_2_PORTAL_FAILURE.ordinal()) {
            return;
        }

        World world = Bukkit.getWorlds().getFirst();
        int x = ThreadLocalRandom.current().nextInt(-1200, 1201);
        int z = ThreadLocalRandom.current().nextInt(-1200, 1201);
        int y = world.getHighestBlockYAt(x, z) + 1;
        fissureManager.spawnSmallFissure(new Location(world, x, y, z));
    }
}
