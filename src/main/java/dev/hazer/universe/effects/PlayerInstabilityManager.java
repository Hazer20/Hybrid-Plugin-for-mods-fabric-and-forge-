package dev.hazer.universe.effects;

import dev.hazer.universe.FracturedUniverse;
import dev.hazer.universe.systems.EventPhaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerInstabilityManager {
    private final FracturedUniverse plugin;
    private final EventPhaseManager phaseManager;
    private final Map<UUID, Long> instabilityStart = new HashMap<>();
    private final BossBar stabilizationBar;

    public PlayerInstabilityManager(FracturedUniverse plugin, EventPhaseManager phaseManager) {
        this.plugin = plugin;
        this.phaseManager = phaseManager;
        this.stabilizationBar = Bukkit.createBossBar("Стабилизация организма", BarColor.WHITE, BarStyle.SEGMENTED_10);
    }

    public void applyInstability(Player player) {
        instabilityStart.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "На тебя наложен эффект Dimension Instability");
        player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 1f, 0.3f);
    }

    public void onReturnStabilization(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 8, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20 * 12, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 14, 1));
        stabilizationBar.addPlayer(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> stabilizationBar.removePlayer(player), 20L * 14);
    }

    public void tickInstability() {
        long stageOne = plugin.getConfig().getLong("нестабильность.стадия1_минут", 20) * 60_000L;
        long stageTwo = plugin.getConfig().getLong("нестабильность.стадия2_минут", 40) * 60_000L;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Long start = instabilityStart.get(player.getUniqueId());
            if (start == null) {
                continue;
            }

            long elapsed = System.currentTimeMillis() - start;
            if (elapsed < stageOne) {
                if (ThreadLocalRandom.current().nextDouble() < 0.18) {
                    player.teleport(player.getLocation().add(ThreadLocalRandom.current().nextInt(-4, 5), 0, ThreadLocalRandom.current().nextInt(-4, 5)));
                    player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 0.7f, 0.5f);
                }
            } else if (elapsed < stageTwo) {
                player.damage(1.0);
                player.sendMessage(ChatColor.RED + "Ваше тело начинает распадаться между вселенными");
                player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 0.8f, 0.7f);
            } else {
                player.damage(2.0);
            }
        }
    }

    public void clear(Player player) {
        instabilityStart.remove(player.getUniqueId());
    }

    public void cleanup() {
        instabilityStart.clear();
        stabilizationBar.removeAll();
    }
}
