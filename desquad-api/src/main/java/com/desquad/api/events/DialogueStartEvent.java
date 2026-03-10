package com.desquad.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class DialogueStartEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String dialogueId;

    public DialogueStartEvent(Player player, String dialogueId) {
        this.player = player;
        this.dialogueId = dialogueId;
    }

    public Player getPlayer() { return player; }
    public String getDialogueId() { return dialogueId; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
