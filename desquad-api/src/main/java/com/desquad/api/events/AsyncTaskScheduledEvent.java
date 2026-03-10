package com.desquad.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Событие постановки heavy-задачи в async очереди ядра. */
public final class AsyncTaskScheduledEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String pool;
    private final String taskName;

    public AsyncTaskScheduledEvent(String pool, String taskName) {
        this.pool = pool;
        this.taskName = taskName;
    }

    public String getPool() { return pool; }
    public String getTaskName() { return taskName; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
