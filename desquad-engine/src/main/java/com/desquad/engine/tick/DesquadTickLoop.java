package com.desquad.engine.tick;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Координатор тик-цикла подсистем DESquadCore.
 */
public final class DesquadTickLoop {
    private final Logger logger;
    private final List<TickSubsystem> subsystems = new CopyOnWriteArrayList<>();
    private volatile long tickCounter;

    public DesquadTickLoop(Logger logger) {
        this.logger = logger;
    }

    public void register(TickSubsystem subsystem) {
        subsystems.add(subsystem);
        logger.info("Tick subsystem registered: " + subsystem.id());
    }

    public long currentTick() {
        return tickCounter;
    }

    public int subsystemCount() {
        return subsystems.size();
    }

    public void pulse() {
        long tick = ++tickCounter;
        for (TickSubsystem subsystem : subsystems) {
            subsystem.onTick(tick);
        }
    }
}
