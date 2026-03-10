package com.desquad.server.runtime;

import com.desquad.api.DESquadAPI;
import com.desquad.engine.tick.DesquadTickLoop;
import com.desquad.engine.tick.TickSubsystem;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Привязка тик-цикла DESquadCore к серверному scheduler Bukkit/Paper.
 */
public final class ServerTickRuntime {
    private final Plugin plugin;
    private final DesquadTickLoop tickLoop;
    private BukkitTask task;

    public ServerTickRuntime(Plugin plugin, DesquadTickLoop tickLoop) {
        this.plugin = plugin;
        this.tickLoop = tickLoop;
    }

    public void registerDefaults() {
        tickLoop.register(new TickSubsystem() {
            @Override public String id() { return "custom-blocks"; }
            @Override public void onTick(long currentTick) {
                DESquadAPI.getPerformanceService().submit("ticks", () -> DESquadAPI.getBlockRegistry().all().size());
            }
        });

        tickLoop.register(new TickSubsystem() {
            @Override public String id() { return "custom-mobs"; }
            @Override public void onTick(long currentTick) {
                DESquadAPI.getPerformanceService().submit("ai", () -> DESquadAPI.getMobRegistry().all().size());
            }
        });
    }

    public void start() {
        stop();
        task = Bukkit.getScheduler().runTaskTimer(plugin, tickLoop::pulse, 1L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
