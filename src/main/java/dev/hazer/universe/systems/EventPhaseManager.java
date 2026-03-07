package dev.hazer.universe.systems;

import dev.hazer.universe.FracturedUniverse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class EventPhaseManager {
    private final FracturedUniverse plugin;
    private EventPhase phase = EventPhase.PHASE_0_NORMAL;
    private final BossBar phaseBar;

    public EventPhaseManager(FracturedUniverse plugin) {
        this.plugin = plugin;
        this.phaseBar = Bukkit.createBossBar("Вселенная стабильна", BarColor.BLUE, BarStyle.SOLID);
    }

    public EventPhase getPhase() {
        return phase;
    }

    public void setPhase(EventPhase phase) {
        this.phase = phase;
        updatePhaseBar();
        plugin.getLogger().info("Event phase switched to " + phase);
    }

    public void startEvent() {
        setPhase(EventPhase.PHASE_1_ANOMALIES);
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[FracturedUniverse] " + ChatColor.LIGHT_PURPLE + "Слой реальности начал дрожать...");
    }

    public void stopEvent() {
        setPhase(EventPhase.PHASE_0_NORMAL);
        phaseBar.removeAll();
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[FracturedUniverse] " + ChatColor.GRAY + "Структура вселенной временно стабилизирована.");
    }

    public void attachPlayer(Player player) {
        phaseBar.addPlayer(player);
    }

    public void detachPlayer(Player player) {
        phaseBar.removePlayer(player);
    }

    public void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!phaseBar.getPlayers().contains(player)) {
                phaseBar.addPlayer(player);
            }
        }
    }

    private void updatePhaseBar() {
        switch (phase) {
            case PHASE_0_NORMAL -> {
                phaseBar.setTitle("Вселенная стабильна");
                phaseBar.setColor(BarColor.BLUE);
                phaseBar.setProgress(1.0);
            }
            case PHASE_1_ANOMALIES -> {
                phaseBar.setTitle("Фаза 1: Аномалии");
                phaseBar.setColor(BarColor.PURPLE);
                phaseBar.setProgress(0.25);
            }
            case PHASE_2_PORTAL_FAILURE -> {
                phaseBar.setTitle("Фаза 2: Портальный сбой");
                phaseBar.setColor(BarColor.RED);
                phaseBar.setProgress(0.5);
            }
            case PHASE_3_STARFALL -> {
                phaseBar.setTitle("Фаза 3: Падение звезды");
                phaseBar.setColor(BarColor.YELLOW);
                phaseBar.setProgress(0.75);
            }
            case PHASE_4_FRACTURE_WAR -> {
                phaseBar.setTitle("Фаза 4: Разломная война");
                phaseBar.setColor(BarColor.PINK);
                phaseBar.setProgress(1.0);
            }
        }
    }
}
