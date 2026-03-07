package dev.hazer.universe.systems;

import dev.hazer.universe.FracturedUniverse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class EventPhaseManager {
    private final FracturedUniverse plugin;
    private EventPhase phase = EventPhase.PHASE_0_NORMAL;

    public EventPhaseManager(FracturedUniverse plugin) {
        this.plugin = plugin;
    }

    public EventPhase getPhase() {
        return phase;
    }

    public void setPhase(EventPhase phase) {
        this.phase = phase;
        plugin.getLogger().info("Фаза ивента: " + phase);
    }

    public void startEvent() {
        setPhase(EventPhase.PHASE_1_ANOMALIES);
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[Вселенная] " + ChatColor.LIGHT_PURPLE + "Слой реальности начал дрожать...");
    }

    public void stopEvent() {
        setPhase(EventPhase.PHASE_0_NORMAL);
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[Вселенная] " + ChatColor.GRAY + "Структура временно стабилизирована.");
    }

    public void attachPlayer(Player player) {
        if (phase != EventPhase.PHASE_0_NORMAL) {
            player.sendMessage(ChatColor.DARK_PURPLE + "Текущая фаза: " + phase);
        }
    }

    public void tick() {
        // зарезервировано для будущих фазовых триггеров
    }
}
