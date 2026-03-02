package com.hazer.hazefishing.manager;

import com.hazer.hazefishing.HazerFishingPlugin;
import com.hazer.hazefishing.model.NFTFish;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class EventManager {
    private final HazerFishingPlugin plugin;

    public EventManager(HazerFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public void broadcastNftCatch(Player player, NFTFish fish) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");
            Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick unfreeze"), 20L);
            player.getWorld().strikeLightningEffect(player.getLocation());
            player.getWorld().spawn(player.getLocation(), org.bukkit.entity.Firework.class, fw -> {});
            Component msg = Component.text("⚡ " + player.getName() + " СОЗДАЛ ЛЕГЕНДУ — NFT РЫБА №" + fish.registryId() + " весом " + String.format("%.2f", fish.weight()) + "г!");
            Bukkit.broadcast(msg);
            BossBar bar = BossBar.bossBar(msg, 1f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.showBossBar(bar);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.8f, 1.1f);
                p.sendTitle("§dNFT LEGEND", "§f" + player.getName(), 10, 60, 10);
            });
            Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getOnlinePlayers().forEach(p -> p.hideBossBar(bar)), 100L);
            plugin.getDiscordWebhookManager().sendNftCatch(player, fish);
        });
    }
}
