package me.adaptiveservercore.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerHeightChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final double oldHeight;
    private double newHeight;
    private boolean cancelled;

    public PlayerHeightChangeEvent(Player player, double oldHeight, double newHeight) {
        this.player = player;
        this.oldHeight = oldHeight;
        this.newHeight = newHeight;
    }

    public Player getPlayer() {
        return player;
    }

    public double getOldHeight() {
        return oldHeight;
    }

    public double getNewHeight() {
        return newHeight;
    }

    public void setNewHeight(double newHeight) {
        this.newHeight = newHeight;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
