package me.adaptiveservercore.height.listener;

import me.adaptiveservercore.AdaptiveServerCore;
import me.adaptiveservercore.height.HeightManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class HeightGameplayListener implements Listener {

    private final AdaptiveServerCore plugin;
    private final HeightManager heightManager;

    public HeightGameplayListener(AdaptiveServerCore plugin, HeightManager heightManager) {
        this.plugin = plugin;
        this.heightManager = heightManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        heightManager.onJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        heightManager.onQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!plugin.getConfig().getBoolean("масштаб-урона-падения", true)) {
            return;
        }

        double scale = heightManager.getHeight(player) / HeightManager.DEFAULT_HEIGHT;
        event.setDamage(event.getDamage() * Math.max(0.15D, scale));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRide(PlayerInteractEntityEvent event) {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("езда-на-игроках.включено", true)) {
            return;
        }

        Player rider = event.getPlayer();
        Entity clicked = event.getRightClicked();

        if (!(clicked instanceof Player target)) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // Разрешаем обычное использование предметов в руке и только пустой рукой начинаем посадку.
        if (rider.getInventory().getItemInMainHand().getType() != Material.AIR) {
            return;
        }

        int maxTower = Math.max(1, config.getInt("езда-на-игроках.максимум-в-стеке", 5));
        int depth = heightManager.getTowerDepth(target);
        if (depth >= maxTower) {
            rider.sendMessage("§cДостигнут лимит башни игроков: §f" + maxTower);
            return;
        }

        if (rider.equals(target) || target.getPassengers().contains(rider) || rider.getVehicle() != null) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean ok = target.addPassenger(rider);
            if (ok) {
                rider.sendMessage("§aВы сели на игрока §f" + target.getName());
                target.sendMessage("§eНа вас сел игрок §f" + rider.getName());
            }
        });
    }
}
