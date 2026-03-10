package com.desquad.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Событие изменения состояния модуля (enabled/disabled). */
public final class ModuleStateChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String moduleId;
    private final boolean enabled;

    public ModuleStateChangeEvent(String moduleId, boolean enabled) {
        this.moduleId = moduleId;
        this.enabled = enabled;
    }

    public String getModuleId() { return moduleId; }
    public boolean isEnabled() { return enabled; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
