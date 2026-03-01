package com.hazer.lightblock.listener;

import com.hazer.lightblock.item.LightBlockItemManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
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
        block.setType(Material.AIR, false);

        ItemStack drop = itemManager.createLightBlockItem(emittedLevel, 1);

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        var leftovers = player.getInventory().addItem(drop);
        leftovers.values().forEach(leftover -> block.getWorld().dropItemNaturally(block.getLocation(), leftover));
    }
}
