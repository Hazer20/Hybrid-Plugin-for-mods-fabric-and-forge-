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
    private final UniverseStabilityManager stabilityManager;
    private final ArgEventManager argEventManager;
    private final ChunkCollapseManager chunkCollapseManager;

    public EventScheduler(FracturedUniverse plugin,
                          EventPhaseManager phaseManager,
                          FissureManager fissureManager,
                          PlayerInstabilityManager instabilityManager,
                          ArgSignalService argSignalService,
                          UniverseStabilityManager stabilityManager,
                          ArgEventManager argEventManager,
                          ChunkCollapseManager chunkCollapseManager) {
        this.plugin = plugin;
        this.phaseManager = phaseManager;
        this.fissureManager = fissureManager;
        this.instabilityManager = instabilityManager;
        this.argSignalService = argSignalService;
        this.stabilityManager = stabilityManager;
        this.argEventManager = argEventManager;
        this.chunkCollapseManager = chunkCollapseManager;
    }

    public void bootstrap() {
        Bukkit.getScheduler().runTaskTimer(plugin, phaseManager::tick, 20L, 20L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, argSignalService::emitServerLogSignal, 20L * 40, 20L * 90);
        Bukkit.getScheduler().runTaskTimer(plugin, argSignalService::playAnomalyPulse, 20L * 20, 20L * 30);
        Bukkit.getScheduler().runTaskTimer(plugin, instabilityManager::tickInstability, 20L, 20L * 5L);

        int degradeEverySec = plugin.getConfig().getInt("вселенная.автоснижение_процентов_каждые_сек", 300);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!stabilityManager.isActive()) {
                return;
            }
            stabilityManager.tick();
            phaseManager.setPhase(stabilityManager.phaseFromStability());
        }, 20L * 30, degradeEverySec * 20L);

        Bukkit.getScheduler().runTaskTimer(plugin, this::spawnRandomFissure, 20L * 60, 20L * 300);
        scheduleAutomaticArg();
        scheduleChunkCollapse();
    }

    public void scheduleStarfall() {
        int min = plugin.getConfig().getInt("вселенная.звезда.минимум_минут", 5);
        int max = plugin.getConfig().getInt("вселенная.звезда.максимум_минут", 20);
        int delayMinutes = ThreadLocalRandom.current().nextInt(min, max + 1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            phaseManager.setPhase(EventPhase.PHASE_3_STARFALL);
            fissureManager.triggerStarfall();
            stabilityManager.setStability(Math.min(stabilityManager.getStability(), 20));
        }, delayMinutes * 60L * 20L);
    }

    public void shutdown() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    private void scheduleAutomaticArg() {
        int min = plugin.getConfig().getInt("arg.авто_интервал_мин", 30);
        int max = plugin.getConfig().getInt("arg.авто_интервал_макс", 120);
        long next = ThreadLocalRandom.current().nextLong(min, max + 1L) * 60L * 20L;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (stabilityManager.isActive()) {
                argEventManager.startRandom();
            }
            scheduleAutomaticArg();
        }, next);
    }

    private void scheduleChunkCollapse() {
        int hours = plugin.getConfig().getInt("вселенная.коллапс_чанков.интервал_часов", 9);
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (stabilityManager.isActive()) {
                chunkCollapseManager.triggerCollapse();
            }
        }, hours * 20L * 3600L, hours * 20L * 3600L);
    }

    private void spawnRandomFissure() {
        if (!plugin.getConfig().getBoolean("разломы.включены", true)) {
            return;
        }
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
