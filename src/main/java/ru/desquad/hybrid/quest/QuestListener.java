package ru.desquad.hybrid.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.desquad.hybrid.DESHybridPlugin;

public class QuestListener implements Listener {

    private final DESHybridPlugin plugin;
    private final QuestManager questManager;

    public QuestListener(DESHybridPlugin plugin, QuestManager questManager) {
        this.plugin = plugin;
        this.questManager = questManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        questManager.ensureQuests(event.getPlayer());
        event.getPlayer().sendMessage("§6[DES] §eТебе доступны мини-квесты: /desquests");
    }
}
