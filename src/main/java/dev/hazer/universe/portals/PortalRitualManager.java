package dev.hazer.universe.portals;

import dev.hazer.universe.FracturedUniverse;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PortalRitualManager {
    private final FracturedUniverse plugin;
    private boolean waitingNetherActivation;

    public PortalRitualManager(FracturedUniverse plugin) {
        this.plugin = plugin;
    }

    public void destroyAllPortals() {
        for (World world : Bukkit.getWorlds()) {
            for (Player player : world.getPlayers()) {
                Location l = player.getLocation();
                world.spawnParticle(Particle.REVERSE_PORTAL, l, 80, 3, 3, 3, 0.06);
                world.playSound(l, Sound.BLOCK_PORTAL_AMBIENT, 1.5f, 0.6f);
            }
        }
        Bukkit.broadcastMessage(ChatColor.RED + "[Вселенная] Все порталы разрушены. Врата закрыты.");
    }

    public void setWaitingNetherActivation(boolean waiting) {
        this.waitingNetherActivation = waiting;
    }

    public boolean isWaitingNetherActivation() {
        return waitingNetherActivation;
    }

    public void onNetherPortalIgnite(Block block, Player who) {
        if (!waitingNetherActivation) {
            return;
        }
        waitingNetherActivation = false;
        Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "[Портал] Зафиксирована попытка открытия. Сбой через 1 секунду...");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.4f);
            block.getWorld().spawnParticle(Particle.FLASH, block.getLocation().add(0.5, 0.5, 0.5), 3, 0.3, 0.3, 0.3, 0.0);
            Bukkit.broadcastMessage(ChatColor.RED + "Аномалия: портал самозакрылся.");
        }, 20L);

        who.sendMessage(ChatColor.GRAY + "End-портал также принудительно закрыт.");
    }
}
