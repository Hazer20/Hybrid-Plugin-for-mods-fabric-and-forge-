package com.desquad.api.events;

import com.desquad.api.items.CustomItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class CustomItemUseEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final CustomItem item;

    public CustomItemUseEvent(Player player, CustomItem item) {
        this.player = player;
        this.item = item;
    }

    public Player getPlayer() { return player; }
    public CustomItem getItem() { return item; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
