package dev.hazer.universe.systems;

import dev.hazer.universe.FracturedUniverse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class ArgSignalService {
    private static final String[] LOG_SIGNALS = new String[]{
            "[WorldIntegrityCheck] Warning: dimension overlap detected",
            "[ArchiveNode-17] unexpected void signature in overworld chunk",
            "[PortalMesh] misaligned anchor vector (delta=3.14159)",
            "[A.R.C.H.I.V.E] emergency rollback denied"
    };

    private final FracturedUniverse plugin;
    private final EventPhaseManager phaseManager;

    public ArgSignalService(FracturedUniverse plugin, EventPhaseManager phaseManager) {
        this.plugin = plugin;
        this.phaseManager = phaseManager;
    }

    public void emitServerLogSignal() {
        int idx = ThreadLocalRandom.current().nextInt(LOG_SIGNALS.length);
        plugin.getLogger().warning(LOG_SIGNALS[idx]);
    }

    public void playAnomalyPulse() {
        if (phaseManager.getPhase().ordinal() < EventPhase.PHASE_1_ANOMALIES.ordinal()) {
            return;
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 0.4f, 0.6f);
            if (ThreadLocalRandom.current().nextDouble() < 0.15) {
                player.sendActionBar(ChatColor.DARK_PURPLE + "Ты слышишь шёпот за пределом мира...");
            }
        });
    }

    public void triggerPortalFailureSequence() {
        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "[ERROR] Unknown dimension");
        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "[ERROR] Portal misalignment");
        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "[ERROR] Reality fragmentation");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcastMessage(ChatColor.RED + "⚠ Нарушилась целостность вселенной ⚠");
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2f, 0.5f);
                player.getWorld().strikeLightningEffect(player.getLocation());
            }
        }, 200L);
    }
}
