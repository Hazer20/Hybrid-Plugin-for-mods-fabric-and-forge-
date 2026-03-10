package com.desquad.api.events;

import com.desquad.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class NPCInteractEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final NPC npc;

    public NPCInteractEvent(Player player, NPC npc) {
        this.player = player;
        this.npc = npc;
    }

    public Player getPlayer() { return player; }
    public NPC getNpc() { return npc; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
