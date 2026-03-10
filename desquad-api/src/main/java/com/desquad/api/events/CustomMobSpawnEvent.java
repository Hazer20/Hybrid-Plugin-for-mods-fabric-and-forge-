package com.desquad.api.events;

import com.desquad.api.mobs.CustomMob;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Событие спавна кастомного моба. */
public final class CustomMobSpawnEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final CustomMob mob;
    private final Location location;

    public CustomMobSpawnEvent(CustomMob mob, Location location) {
        this.mob = mob;
        this.location = location;
    }

    public CustomMob getMob() { return mob; }
    public Location getLocation() { return location; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
