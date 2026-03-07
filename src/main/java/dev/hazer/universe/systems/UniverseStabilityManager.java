package dev.hazer.universe.systems;

import dev.hazer.universe.FracturedUniverse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class UniverseStabilityManager {
    private final FracturedUniverse plugin;
    private final BossBar bossBar;
    private boolean active;
    private int stability;

    public UniverseStabilityManager(FracturedUniverse plugin) {
        this.plugin = plugin;
        this.stability = plugin.getConfig().getInt("вселенная.стартовая_стабильность", 100);
        this.bossBar = Bukkit.createBossBar("Состояние вселенной", BarColor.BLUE, BarStyle.SOLID);
        this.bossBar.setVisible(false);
    }

    public void start() {
        active = true;
        stability = plugin.getConfig().getInt("вселенная.стартовая_стабильность", 100);
        updateBar();
        bossBar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
    }

    public void stop() {
        active = false;
        bossBar.setVisible(false);
        bossBar.removeAll();
        stability = plugin.getConfig().getInt("вселенная.стартовая_стабильность", 100);
    }

    public void attachPlayer(Player player) {
        if (active) {
            bossBar.addPlayer(player);
        }
    }

    public void tick() {
        if (!active) {
            return;
        }
        int step = plugin.getConfig().getInt("вселенная.шаг_снижения", 2);
        setStability(Math.max(0, stability - step));
    }

    public void setStability(int value) {
        stability = Math.max(0, Math.min(100, value));
        updateBar();
    }

    public int getStability() {
        return stability;
    }

    public boolean isActive() {
        return active;
    }

    public EventPhase phaseFromStability() {
        if (stability <= 0) return EventPhase.PHASE_4_FRACTURE_WAR;
        if (stability <= 20) return EventPhase.PHASE_3_STARFALL;
        if (stability <= 40) return EventPhase.PHASE_2_PORTAL_FAILURE;
        if (stability <= 60) return EventPhase.PHASE_2_PORTAL_FAILURE;
        if (stability <= 80) return EventPhase.PHASE_1_ANOMALIES;
        return EventPhase.PHASE_0_NORMAL;
    }

    private void updateBar() {
        bossBar.setTitle(ChatColor.LIGHT_PURPLE + "Состояние вселенной: " + ChatColor.WHITE + stability + "%");
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, stability / 100.0)));

        if (stability > 80) {
            bossBar.setColor(BarColor.BLUE);
        } else if (stability > 60) {
            bossBar.setColor(BarColor.GREEN);
        } else if (stability > 40) {
            bossBar.setColor(BarColor.YELLOW);
        } else if (stability > 20) {
            bossBar.setColor(BarColor.RED);
        } else {
            bossBar.setColor(BarColor.PURPLE);
        }

        bossBar.setVisible(true);
    }
}
