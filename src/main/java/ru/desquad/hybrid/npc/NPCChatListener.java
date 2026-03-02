package ru.desquad.hybrid.npc;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.desquad.hybrid.DESHybridPlugin;

import java.util.List;

public class NPCChatListener implements Listener {

    private final DESHybridPlugin plugin;
    private final BuilderNPCManager npcManager;

    public NPCChatListener(DESHybridPlugin plugin, BuilderNPCManager npcManager) {
        this.plugin = plugin;
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (!npcManager.isNearNPC(event.getPlayer())) {
            return;
        }
        String message = event.message().toString().toLowerCase();
        List<String> phrases = plugin.getConfig().getStringList("npc-builder.command-phrases");
        boolean ok = phrases.stream().anyMatch(phrase -> message.contains(phrase.toLowerCase()));
        if (!ok) {
            return;
        }
        event.getPlayer().getScheduler().run(plugin, task -> {
            npcManager.acceptOrder(event.getPlayer());
            npcManager.npcComment(event.getPlayer());
        }, null);
    }
}
