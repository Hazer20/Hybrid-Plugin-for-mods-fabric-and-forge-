package com.hazer.lightblock.listener;

import com.hazer.lightblock.item.LightBlockItemManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class LightBlockListener implements Listener {

    private final LightBlockItemManager itemManager;

    public LightBlockListener(LightBlockItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack hand = event.getItemInHand();
        if (!itemManager.isLightBlockItem(hand)) {
            return;
        }

        Block block = event.getBlockPlaced();
        if (block.getType() != Material.LIGHT) {
            block.setType(Material.LIGHT, false);
        }

        int level = 1;

        if (block.getBlockData() instanceof Levelled levelled) {
            int max = levelled.getMaximumLevel();
            int bukkitLevel = Math.max(0, Math.min(max, 15 - level));
            levelled.setLevel(bukkitLevel);
            block.setBlockData(levelled, false);
        }

        event.getPlayer().playSound(block.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_PLACE, 0.65f, 1.6f);
        block.getWorld().spawnParticle(Particle.GLOW, block.getLocation().add(0.5, 0.5, 0.5), 14, 0.25, 0.25, 0.25, 0.02);
        block.getWorld().spawnParticle(Particle.END_ROD, block.getLocation().add(0.5, 0.6, 0.5), 8, 0.18, 0.18, 0.18, 0.01);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.LIGHT) {
            return;
        }

        Player player = event.getPlayer();
        int emittedLevel = 1;

        event.setDropItems(false);
        block.getWorld().spawnParticle(Particle.FLASH, block.getLocation().add(0.5, 0.5, 0.5), 1, 0.0, 0.0, 0.0, 0.0);
        block.getWorld().spawnParticle(Particle.GLOW, block.getLocation().add(0.5, 0.5, 0.5), 20, 0.3, 0.3, 0.3, 0.01);
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.6f, 1.9f);
        block.setType(Material.AIR, false);

        ItemStack drop = itemManager.createLightBlockItem(emittedLevel, 1);

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        var leftovers = player.getInventory().addItem(drop);
        leftovers.values().forEach(leftover -> block.getWorld().dropItemNaturally(block.getLocation(), leftover));
    }
}
