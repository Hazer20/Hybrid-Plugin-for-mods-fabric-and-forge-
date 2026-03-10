package com.desquad.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class DialogueChoiceEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String dialogueId;
    private final String choiceId;

    public DialogueChoiceEvent(Player player, String dialogueId, String choiceId) {
        this.player = player;
        this.dialogueId = dialogueId;
        this.choiceId = choiceId;
    }

    public Player getPlayer() { return player; }
    public String getDialogueId() { return dialogueId; }
    public String getChoiceId() { return choiceId; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
