package ru.desquad.hybrid.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class QuestListener implements Listener {

    private final QuestManager questManager;

    public QuestListener(QuestManager questManager) {
        this.questManager = questManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        questManager.ensureQuests(event.getPlayer());
        event.getPlayer().sendMessage("§6[DES] §eТебе доступны мини-квесты: /desquests");
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        questManager.onBlockBreak(event.getPlayer(), event.getBlock().getType());
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        questManager.onMobKill(killer, event.getEntityType());
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            questManager.onFishCatch(event.getPlayer());
        }
    }
}
