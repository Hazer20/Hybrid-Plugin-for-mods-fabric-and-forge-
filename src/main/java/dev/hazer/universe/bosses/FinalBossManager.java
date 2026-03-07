package dev.hazer.universe.bosses;

import dev.hazer.universe.FracturedUniverse;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class FinalBossManager {
    private final FracturedUniverse plugin;
    private LivingEntity currentBoss;

    public FinalBossManager(FracturedUniverse plugin) {
        this.plugin = plugin;
    }

    public void spawnFinalBoss(Location location) {
        World world = location.getWorld();
        currentBoss = (LivingEntity) world.spawnEntity(location, EntityType.WITHER);
        currentBoss.customName(Component.text("The Fractured Architect"));
        currentBoss.setCustomNameVisible(true);
        currentBoss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(1200.0);
        currentBoss.setHealth(1200.0);

        Bukkit.broadcastMessage(ChatColor.DARK_RED + "Финал сезона начался: The Fractured Architect пробудился.");
        scheduleAbilities();
    }

    public void finishSeason() {
        if (currentBoss != null && !currentBoss.isDead()) {
            currentBoss.remove();
        }
        Bukkit.broadcastMessage(ChatColor.GREEN + "Структура реальности восстановлена");
    }

    private void scheduleAbilities() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (currentBoss == null || currentBoss.isDead()) {
                return;
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                switch (ThreadLocalRandom.current().nextInt(5)) {
                    case 0 -> player.sendMessage(ChatColor.DARK_PURPLE + "Разлом реальности искажает пространство вокруг тебя.");
                    case 1 -> player.sendMessage(ChatColor.GRAY + "Блоки рядом начинают исчезать...");
                    case 2 -> player.teleport(player.getLocation().add(ThreadLocalRandom.current().nextInt(-10, 11), 5, ThreadLocalRandom.current().nextInt(-10, 11)));
                    case 3 -> player.setGravity(false);
                    case 4 -> player.sendMessage(ChatColor.DARK_AQUA + "Время идет рывками. Мир тормозит.");
                    default -> {}
                }
                Bukkit.getScheduler().runTaskLater(plugin, () -> player.setGravity(true), 60L);
            }
        }, 120L, 200L);
    }
}
