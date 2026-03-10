package com.desquad.engine.dialogue;

import com.desquad.api.dialogue.DialogueService;
import org.bukkit.entity.Player;

public final class SimpleDialogueService implements DialogueService {
    @Override
    public void startDialogue(Player player, String dialogueId) {
        player.sendMessage("§e[Dialogue] §fStarted: " + dialogueId);
    }
}
