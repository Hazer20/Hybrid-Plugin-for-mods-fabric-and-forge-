package com.desquad.api.events;

import com.desquad.api.blocks.CustomBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Событие установки кастомного блока. */
public final class CustomBlockPlaceEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final CustomBlock block;
    private final Location location;

    public CustomBlockPlaceEvent(Player player, CustomBlock block, Location location) {
        this.player = player;
        this.block = block;
        this.location = location;
    }

    public Player getPlayer() { return player; }
    public CustomBlock getBlock() { return block; }
    public Location getLocation() { return location; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
